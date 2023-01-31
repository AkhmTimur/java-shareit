package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRequestDtoMapper itemRequestDtoMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemDtoMapper itemDtoMapper;
    @InjectMocks
    private ItemRequestService itemRequestService;

    LocalDateTime now = LocalDateTime.now().withNano(0);
    User user = new User(1L, "@mail.ru", "name");
    ItemRequest itemRequest = new ItemRequest(1L, "description", user, now);
    Item itemToSave = new Item(1L, "name", "description", true, user, itemRequest);

    ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "description", now, Collections.emptyList());
    ItemDto itemDto = new ItemDto(1L, "name", "description", true, user.getId(), Collections.emptyList(), itemRequest.getId());


    @BeforeEach
    void setup() {
        lenient().when(itemRequestDtoMapper.itemRequestToDto(itemRequest))
                .thenReturn(new ItemRequestDto(itemRequest.getId(), itemRequest.getDescription(), itemRequest.getCreated()));
        lenient().when(itemRequestDtoMapper.dtoToItemRequest(itemRequestDto, user))
                .thenReturn(new ItemRequest(itemRequestDto.getDescription(), user));
        lenient().when(itemDtoMapper.itemToDto(itemToSave))
                .thenReturn(new ItemDto(itemToSave.getId(), itemToSave.getName(), itemToSave.getDescription(), itemToSave.getAvailable(), itemToSave.getOwner().getId(), Collections.emptyList(), itemRequest.getId()));
        lenient().when(itemDtoMapper.dtoToItem(itemDto, user))
                .thenReturn(new Item(itemDto.getId(), itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable(), user, new ItemRequest()));

    }

    @Test
    void createItemRequest_whenUserFoundAndDescriptionCorrect_thenCreateItemRequest() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(new ItemRequest(null, "description", user, any())))
                .thenReturn(new ItemRequest(1L, "description", user, LocalDateTime.now()));

        itemRequestService.createItemRequest(user.getId(), itemRequestDto);

        verify(itemRequestRepository).save(new ItemRequest(null, "description", user, any()));
    }

    @Test
    void createItemRequest_whenItemOwnerDoesntExistsAndDescriptionCorrect_thenThrowException() {
        when(userRepository.findById(user.getId())).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> itemRequestService.createItemRequest(user.getId(), itemRequestDto));
        verify(itemRequestRepository, never()).save(itemRequest);
    }

    @Test
    void createItemRequest_whenDescriptionIsNull_thenThrowException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestService.createItemRequest(user.getId(), itemRequestDto)).thenThrow(IncorrectDataException.class);

        assertThrows(IncorrectDataException.class, () -> itemRequestService.createItemRequest(user.getId(), itemRequestDto));
        verify(itemRequestRepository, never()).save(itemRequest);
    }

    @Test
    void getItemRequests_whenUserExists_thenReturnListItemRequestDto() {
        List<Item> itemList = List.of(itemToSave);
        itemRequestDto.setItems(List.of(itemDto));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findByRequestIdIn(List.of(user.getId()))).thenReturn(itemList);
        when(itemRequestRepository.findByRequesterId(user.getId())).thenReturn(List.of(itemRequest));

        List<ItemRequestDto> result = itemRequestService.getItemRequests(user.getId());
        assertEquals(result, List.of(itemRequestDto));
        verify(itemRepository).findByRequestIdIn(List.of(1L));
    }

    @Test
    void getItemRequests_whenUserDoesntExists_thenThrowException() {
        when(userRepository.findById(user.getId())).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> itemRequestService.getItemRequests(user.getId()));
        verify(itemRequestRepository, never()).save(itemRequest);
    }

    @Test
    void getAllItemRequest_whenUserExistsAndCorrectPaginationData_thenReturnListItemRequestDto() {
        List<Item> itemList = List.of(itemToSave);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterIdIsNot(user)).thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestIdIn(List.of(user.getId()))).thenReturn(itemList);

        List<ItemRequestDto> result = itemRequestService.getAllItemRequest(user.getId(), 0, 1);

        assertEquals(result, List.of(new ItemRequestDto(1L, "description", now, List.of(itemDto))));
        verify(itemRequestRepository).findAllByRequesterIdIsNot(user);
    }

    @Test
    void getAllItemRequest_whenUserDoesntExists_thenThrowException() {
        when(userRepository.findById(user.getId())).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> itemRequestService.getAllItemRequest(1L, 0, 1));
    }

    @Test
    void getAllItemRequest_whenPaginationDataIncorrect_thenThrowException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        lenient().when(itemRequestService.getAllItemRequest(user.getId(), 0, 1)).thenThrow(IncorrectDataException.class);

        assertThrows(IncorrectDataException.class, () -> itemRequestService.getAllItemRequest(user.getId(), 0, 0));
    }

    @Test
    void getItemRequest_whenUserFoundAndItemRequestFound_thenReturnItemRequestDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));

        ItemRequestDto result = itemRequestService.getItemRequest(user.getId(), 1L);
        assertEquals(result, itemRequestDto);
    }

    @Test
    void getItemRequest_whenUserNotFound_thenThrowException() {
        when(userRepository.findById(user.getId())).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> itemRequestService.getItemRequest(user.getId(), 1L));
    }

    @Test
    void getItemRequest_whenItemRequestNotFound_thenThrowException() {
        lenient().when(itemRequestRepository.findById(1L)).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> itemRequestService.getItemRequest(user.getId(), 1L));
    }
}

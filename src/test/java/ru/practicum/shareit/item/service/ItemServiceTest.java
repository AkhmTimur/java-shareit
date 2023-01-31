package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.comments.dto.CommentDtoMapper;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemDtoMapper itemDtoMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingDtoMapper bookingDtoMapper;
    @Mock
    private CommentDtoMapper commentDtoMapper;
    @InjectMocks
    private ItemService itemService;
    User user = new User(1L, "email@mail.com", "name");
    ItemRequest itemRequest = new ItemRequest("description", user);
    Item itemToSave = new Item(1L, "name", "description", true, user, itemRequest);
    ItemDto itemDto = new ItemDto(1L, "name", "description", true, user.getId(), Collections.emptyList(), itemRequest.getId());


    @BeforeEach
    void setup() {
        lenient().when(itemDtoMapper.itemToDto(itemToSave))
                .thenReturn(new ItemDto(itemToSave.getId(), itemToSave.getName(), itemToSave.getDescription(), itemToSave.getAvailable(), itemToSave.getOwner().getId(), Collections.emptyList(), itemRequest.getId()));
        lenient().when(itemDtoMapper.dtoToItem(itemDto, user))
                .thenReturn(new Item(itemDto.getId(), itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable(), user, new ItemRequest()));
    }

    @Test
    void createItem_whenOwnerFound_thenSaveItem() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemService.createItem(itemDto)).thenReturn(itemDto);

        ItemDto actualItem = itemService.createItem(itemDto);

        assertEquals(itemToSave.getId(), actualItem.getId());
    }

    @Test
    void createItem_whenOwnerNotFound_thenThrowException() {
        when(userRepository.findById(user.getId())).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> itemService.createItem(itemDto));
    }

    @Test
    void updateItem_whenItemAndUserIsPresent_thenUpdate() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemDto.getId())).thenReturn(Optional.of(itemToSave));
        when(itemService.updateItem(itemDto)).thenReturn(itemDto);

        ItemDto actualItemDto = itemService.updateItem(itemDto);

        assertEquals(itemDto, actualItemDto);
    }

    @Test
    void updateItem_whenItemNotFound_thenThrowException() {
        lenient().when(itemRepository.findById(itemDto.getId())).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> itemService.updateItem(itemDto));
    }

    @Test
    void updateItem_whenUserNotFound_thenThrowException() {
        lenient().when(userRepository.findById(user.getId())).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> itemService.updateItem(itemDto));
    }

    @Test
    void updateItem_whenItemDtoOwnerNotEqualItemOwner_thenThrowException() {
        when(itemRepository.findById(itemDto.getId())).thenReturn(Optional.of(itemToSave));
        when(userRepository.findById(itemDto.getOwnerId())).thenReturn(Optional.of(new User(99L, "e@mail.ru", "name")));
        when(itemService.updateItem(itemDto)).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> itemService.updateItem(itemDto));
    }

    @Test
    void getItemById_whenItemIsPresentAndUsersEquals_thenReturnItem() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        Booking booking = new Booking(1L, now, tomorrow, itemToSave, user, BookingStatus.APPROVED);
        Long itemId = itemToSave.getId();
        Long userId = user.getId();
        Comment comment = new Comment(0L, "text", itemToSave, user);
        CommentDto commentDto = new CommentDto(0L, "text", user.getName(), null);
        lenient().when(commentDtoMapper.commentToDto(comment)).thenReturn(commentDto);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToSave));
        when(bookingRepository.findByItemIdAndItemOwnerId(itemId, userId)).thenReturn(List.of(booking));
        when(commentRepository.findByItemId(itemId)).thenReturn(Collections.emptyList());

        ItemDto result = itemService.getItemById(itemDto.getId(), user.getId());

        assertEquals(result, itemDto);
    }

    @Test
    void getItemById_whenItemIsNotPresentAndUsersEquals_thenReturnItem() {
        Long itemId = itemToSave.getId();
        when(itemRepository.findById(itemId)).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> itemService.getItemById(itemDto.getId(), user.getId()));
    }

    @Test
    void getItemById_whenItemIsPresentAndUsersNotEquals_thenReturnItemWithoutCommentsAndBookings() {
        Long itemId = itemToSave.getId();
        Long userId = 99L;
        ItemDto itemWithoutCommentsAndBookings = new ItemDto(itemToSave.getId(), itemToSave.getName(),
                itemToSave.getDescription(), itemToSave.getAvailable(), userId, Collections.emptyList(), null);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToSave));
        when(itemService.getItemById(itemToSave.getId(), userId)).thenReturn(itemWithoutCommentsAndBookings);

        ItemDto result = itemService.getItemById(itemToSave.getId(), userId);

        assertEquals(result, itemWithoutCommentsAndBookings);
    }

    @Test
    void getAllItemsOfUser_whenOk_thenReturnListDto() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        Long userId = user.getId();
        List<ItemDto> expected = List.of(itemDto);
        Comment comment = new Comment(0L, "text", itemToSave, user);
        CommentDto commentDto = new CommentDto(0L, "text", user.getName(), null);
        itemDto.setComments(List.of(new CommentDto(0L, "text", user.getName(), null)));
        Booking booking = new Booking(1L, now, tomorrow, itemToSave, user, BookingStatus.APPROVED);
        lenient().when(commentDtoMapper.commentToDto(comment)).thenReturn(commentDto);
        when(itemRepository.findByOwnerIdOrderById(userId)).thenReturn(List.of(itemToSave));
        when(bookingRepository.findByItemIdIn(List.of(itemToSave.getId()))).thenReturn(List.of(booking));
        when(commentRepository.findByItemIdIn(List.of(itemToSave.getId()))).thenReturn(List.of(comment));

        List<ItemDto> result = itemService.getAllItemsOfUser(userId);

        assertEquals(expected, result);
    }

    @Test
    void searchForItem_whenOk_thenReturnListDto() {
        String searchCriteria = "thing";
        List<ItemDto> expected = List.of(itemDto);
        lenient().when(itemRepository.searchItems(searchCriteria)).thenReturn(List.of(itemToSave));

        List<ItemDto> result = itemService.searchForItem(searchCriteria);

        assertEquals(expected, result);
    }

    @Test
    void searchForItem_whenCriteriaIsBlank_thenReturnEmptyList() {
        String searchCriteria = "";
        List<ItemDto> expected = Collections.emptyList();
        lenient().when(itemRepository.searchItems(searchCriteria)).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.searchForItem(searchCriteria);

        assertEquals(expected, result);
    }

    @Test
    void createCommentToItem_whenBookingListGreaterThenZeroAndCommentIsNotNull_thenReturnComment() {
        LocalDateTime now = LocalDateTime.now().minusDays(1);
        LocalDateTime yesterday = now.minusDays(1);
        Long userId = user.getId();
        Long itemId = itemToSave.getId();
        Booking booking = new Booking(1L, yesterday, now, itemToSave, user, BookingStatus.APPROVED);
        CommentDto expected = new CommentDto(1L, "text", user.getName(), LocalDate.now());
        lenient().when(commentDtoMapper.commentToDto(any(Comment.class))).thenReturn(expected);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToSave));
        when(userRepository.findById(itemId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemIdAndBookerId(itemId, userId)).thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment(1L, "text", itemToSave, user));

        CommentDto result = itemService.createCommentToItem(itemId, "text", userId);

        assertEquals(expected.getText(), result.getText());
    }

    @Test
    void createCommentToItem_whenBookingListAreEmpty_thenThrowException() {
        Long userId = user.getId();
        Long itemId = itemToSave.getId();
        when(bookingRepository.findByItemIdAndBookerId(itemId, userId)).thenReturn(Collections.emptyList());

        assertThrows(IncorrectDataException.class, () -> itemService.createCommentToItem(itemId, "text", userId));
    }
}

package ru.practicum.shareit.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestDtoMapper itemRequestDtoMapper;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemDtoMapper itemDtoMapper;

    public ItemRequestService(ItemRequestRepository itemRequestRepository, ItemRequestDtoMapper itemRequestDtoMapper,
                              UserRepository userRepository,
                              ItemRepository itemRepository, ItemDtoMapper itemDtoMapper) {
        this.itemRequestRepository = itemRequestRepository;
        this.itemRequestDtoMapper = itemRequestDtoMapper;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.itemDtoMapper = itemDtoMapper;
    }

    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        ItemRequest itemRequest = itemRequestDtoMapper.dtoToItemRequest(itemRequestDto, user);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequestDtoMapper.itemRequestToDto(itemRequestRepository.save(itemRequest));
    }

    public List<ItemRequestDto> getItemRequests(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId);
        return getItemRequestDtos(itemRequests);
    }

    public List<ItemRequestDto> getAllItemRequest(Long userId, Integer from, Integer size) {
        User user = userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        if (from < 0 || size < 1) {
            throw new IncorrectDataException("Переданы некорректные данные");
        }
        List<ItemRequest> itemRequests;
        itemRequests = itemRequestRepository.findAllByRequesterIdIsNot(user, PageRequest.of(from, size));
        return getItemRequestDtos(itemRequests);
    }

    private List<ItemRequestDto> getItemRequestDtos(List<ItemRequest> itemRequests) {
        List<ItemRequestDto> result = itemRequests
                .stream()
                .map(itemRequestDtoMapper::itemRequestToDto)
                .collect(Collectors.toList());
        List<Long> ids = itemRequests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        Map<Long, List<ItemDto>> itemDtos = itemRepository.findByRequestIdIn(ids)
                .stream()
                .map(itemDtoMapper::itemToDto).collect(Collectors.groupingBy(ItemDto::getRequestId));
        for (ItemRequestDto itemRequestDto : result) {
            itemRequestDto.setItems(itemDtos.getOrDefault(itemRequestDto.getId(), Collections.emptyList()));
        }
        return result;
    }

    public ItemRequestDto getItemRequest(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        ItemRequest itemRequests = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Данные не найдены"));
        return getItemRequestDtos(List.of(itemRequests)).get(0);
    }
}

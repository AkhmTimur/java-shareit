package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.comments.dto.CommentDtoMapper;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemDtoMapper itemDtoMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final BookingDtoMapper bookingDtoMapper;
    private final CommentDtoMapper commentDtoMapper;

    @Transactional
    public ItemDto createItem(ItemDto itemDto) {
        Long ownerId = itemDto.getOwnerId();
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        return itemDtoMapper.itemToDto(itemRepository.save(itemDtoMapper.dtoToItem(itemDto, user)));
    }

    @Transactional
    public ItemDto updateItem(ItemDto itemDto) {
        Item item = itemRepository.findById(itemDto.getId())
                .orElseThrow(() -> new DataNotFoundException("Предмет не найден"));
        Long ownerId = itemDto.getOwnerId();
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        if (!Objects.equals(item.getOwner().getId(), ownerId)) {
            throw new DataNotFoundException("Предмет " + itemDto + " с владельцем " + ownerId + " не найден");
        }
        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(item.getAvailable());
        }
        if (itemDto.getDescription() == null) {
            itemDto.setDescription(item.getDescription());
        }
        if (itemDto.getName() == null) {
            itemDto.setName(item.getName());
        }
        return itemDtoMapper.itemToDto(itemRepository.save(
                itemDtoMapper.dtoToItem(itemDto, user)));
    }

    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new DataNotFoundException("Предмет не найден"));
        ItemDto itemDto = itemDtoMapper.itemToDto(item);
        if (item.getOwner().getId().equals(userId)) {
            List<Booking> bookingList = bookingRepository.findByItemIdAndItemOwnerId(itemId, userId)
                    .stream()
                    .filter(b -> !b.getStatus().equals(BookingStatus.REJECTED))
                    .collect(Collectors.toList());
            Map<Long, List<Booking>> bookingMap = createItemBookingsMap(bookingList);
            List<Comment> comments = commentRepository.findByItemId(item.getId());
            List<CommentDto> commentDtos = comments.stream().map(commentDtoMapper::commentToDto).collect(Collectors.toList());
            itemDto.setComments(commentDtos);
            addLastAndNextBookingToItem(itemDto, bookingMap.get(itemDto.getId()));
        }
        return itemDto;
    }

    public List<ItemDto> getAllItemsOfUser(Long userId, Integer from, Integer size) {
        List<ItemDto> itemDtos = new ArrayList<>();
        List<Item> itemList = itemRepository.findByOwnerIdOrderById(userId, PageRequest.of(from, size));
        List<Long> ids = itemList.stream().map(Item::getId).collect(Collectors.toList());
        List<Booking> bookingList = bookingRepository.findByItemIdIn(ids);
        Map<Long, List<Booking>> bookingMap = createItemBookingsMap(bookingList);

        Map<Long, List<Comment>> comments = commentRepository.findByItemIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));
        for (Item item : itemList) {
            ItemDto itemDto = itemDtoMapper.itemToDto(item);
            itemDtos.add(itemDto);
            addLastAndNextBookingToItem(itemDto, bookingMap.get(item.getId()));
        }
        setCommentsToDto(itemDtos, comments);
        return itemDtos;
    }

    private Map<Long, List<Booking>> createItemBookingsMap(List<Booking> itemBookings) {
        return itemBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));
    }

    private void setCommentsToDto(List<ItemDto> itemDtos, Map<Long, List<Comment>> commentDtos) {
        for (ItemDto itemDto : itemDtos) {
            if (commentDtos.containsKey(itemDto.getId())) {
                itemDto.setComments(
                        commentDtos.get(itemDto.getId())
                                .stream()
                                .map(commentDtoMapper::commentToDto)
                                .collect(Collectors.toList())
                );
            }

        }
    }

    public List<ItemDto> searchForItem(String searchCriteria, Integer from, Integer size) {
        List<ItemDto> itemDtos = new ArrayList<>();
        if (searchCriteria.isBlank()) {
            return Collections.emptyList();
        } else {
            for (Item item : itemRepository.searchItems(searchCriteria, PageRequest.of(from, size))) {
                itemDtos.add(itemDtoMapper.itemToDto(item));
            }
            return itemDtos;
        }
    }

    private void addLastAndNextBookingToItem(ItemDto itemDto, List<Booking> bookingList) {
        if (bookingList != null) {
            addLastBooking(itemDto, bookingList);
            addNextBooking(itemDto, bookingList);
        }
    }

    private void addNextBooking(ItemDto itemDto, List<Booking> bookingList) {
        bookingList.sort((b1, b2) -> b2.getStartDate().compareTo(b1.getStartDate()));
        List<Booking> filtered = bookingList
                .stream()
                .filter(b -> b.getStartDate().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (filtered.size() > 0) {
            itemDto.setNextBooking(bookingDtoMapper.bookingToDto(filtered.get(0)));
        }
    }

    private void addLastBooking(ItemDto itemDto, List<Booking> bookingList) {
        bookingList.sort((b1, b2) -> b1.getEndDate().compareTo(b2.getEndDate()));
        List<Booking> filtered = bookingList
                .stream()
                .filter(b -> b.getStartDate().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (filtered.size() > 0) {
            itemDto.setLastBooking(bookingDtoMapper.bookingToDto(filtered.get(0)));
        }
    }

    @Transactional
    public CommentDto createCommentToItem(Long itemId, String text, Long userId) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Предмет не найден"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        List<Booking> bookingList = bookingRepository.findByItemIdAndBookerId(itemId, userId)
                .stream()
                .filter(b -> b.getStatus().equals(BookingStatus.APPROVED))
                .filter(b -> b.getEndDate().isBefore(currentDateTime))
                .collect(Collectors.toList());
        CommentDto result = null;
        if (bookingList.size() > 0) {
            if (!text.isBlank()) {
                Optional<Comment> commentByItemAndUser = commentRepository.findByItemIdAndAuthorId(item.getId(),
                        user.getId());
                if (commentByItemAndUser.isEmpty()) {
                    Comment comment = new Comment(text, item, user);
                    Comment c = commentRepository.save(comment);
                    result = commentDtoMapper.commentToDto(c);
                }
            }
        }
        if (result != null) {
            return result;
        } else {
            throw new IncorrectDataException("Переданы некорректные данные");
        }
    }
}

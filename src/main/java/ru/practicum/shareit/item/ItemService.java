package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.comments.dto.CommentDtoMapper;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
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
        userRepository.findById(ownerId).orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        return itemDtoMapper.itemToDto(itemRepository.save(itemDtoMapper.dtoToItem(itemDto)));
    }

    @Transactional
    public ItemDto updateItem(ItemDto itemDto) {
        Optional<Item> item = itemRepository.findById(itemDto.getId());
        if (item.isPresent()) {
            Item itemGet = item.get();
            Long ownerId = itemDto.getOwnerId();
            if (!Objects.equals(itemGet.getOwner().getId(), ownerId)) {
                throw new DataNotFoundException("Предмет " + itemDto + " с владельцем " + ownerId + " не найден");
            }
            if (itemDto.getAvailable() == null) {
                itemDto.setAvailable(itemGet.getAvailable());
            }
            if (itemDto.getDescription() == null) {
                itemDto.setDescription(itemGet.getDescription());
            }
            if (itemDto.getName() == null) {
                itemDto.setName(itemGet.getName());
            }
            return itemDtoMapper.itemToDto(itemRepository.save(
                    itemDtoMapper.dtoToItem(itemDto)));
        } else {
            throw new DataNotFoundException("Предмет не найден");
        }
    }

    public ItemDto getItemById(Long itemId, Long userId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            ItemDto itemDto = itemDtoMapper.itemToDto(item.get());
            if (item.get().getOwner().getId().equals(userId)) {
                List<Booking> bookingList = bookingRepository.findByItemIdAndItemOwnerId(itemId, userId);
                bookingList.removeIf(booking -> booking.getStatus().equals(BookingStatus.REJECTED));
                Map<Long, List<Booking>> bookingMap = createItemBookingsMap(bookingList);
                List<Comment> comments = commentRepository.findByItemId(item.get().getId());
                List<CommentDto> commentDtos = comments.stream().map(commentDtoMapper::commentToDto).collect(Collectors.toList());
                itemDto.setComments(commentDtos);
                addLastAndNextBookingToItem(itemDto, bookingMap.get(itemDto.getId()));
            }
            return itemDto;
        } else {
            throw new DataNotFoundException("Предмет не найден");
        }
    }

    public List<ItemDto> getAllItemsOfUser(Long userId) {
        List<ItemDto> itemDtos = new ArrayList<>();
        List<Item> itemList = itemRepository.findByOwnerIdOrderById(userId);
        List<Long> ids = itemList.stream().map(Item::getId).collect(Collectors.toList());
        List<Booking> bookingList = bookingRepository.findByItemIdIn(ids);
        Map<Long, List<Booking>> bookingMap = createItemBookingsMap(bookingList);

        List<Comment> comments = commentRepository.findByItemIdIn(itemList.stream().map(Item::getId).collect(Collectors.toList()));
        for (Item item : itemList) {
            ItemDto itemDto = itemDtoMapper.itemToDto(item);
            itemDtos.add(itemDto);
            addLastAndNextBookingToItem(itemDto, bookingMap.get(item.getId()));
        }
        setCommentsToDto(itemDtos, comments);
        return itemDtos;
    }

    private Map<Long, List<Booking>> createItemBookingsMap(List<Booking> itemBookings) {
        Map<Long, List<Booking>> result = new HashMap<>();
        for (Booking booking : itemBookings) {
            List<Booking> itemBooking = itemBookings.stream().filter(b -> b.getItem().getId().equals(booking.getItem().getId())).collect(Collectors.toList());
            result.put(booking.getItem().getId(), itemBooking);
        }
        return result;
    }

    private void setCommentsToDto(List<ItemDto> itemDtos, List<Comment> commentDtos) {
        for (ItemDto itemDto : itemDtos) {
            List<Comment> itemComments = commentDtos
                    .stream()
                    .filter(c -> itemDto.getId().equals(c.getItem().getId()))
                    .collect(Collectors.toList());
            itemDto.setComments(itemComments.stream().map(commentDtoMapper::commentToDto).collect(Collectors.toList()));
        }
    }

    public List<ItemDto> searchForItem(String searchCriteria) {
        List<ItemDto> itemDtos = new ArrayList<>();
        if (searchCriteria.isBlank()) {
            return Collections.emptyList();
        } else {
            for (Item item : itemRepository.searchItems(searchCriteria)) {
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
                .filter(b -> b.getEndDate().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (filtered.size() > 0) {
            itemDto.setLastBooking(bookingDtoMapper.bookingToDto(filtered.get(0)));
        }
    }

    @Transactional
    public CommentDto createCommentToItem(Long itemId, String text, Long userId) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Optional<Item> item = itemRepository.findById(itemId);
        Optional<User> user = userRepository.findById(userId);
        List<Booking> bookingList = bookingRepository.findByItemIdAndBookerId(itemId, userId)
                .stream()
                .filter(b -> b.getStatus().equals(BookingStatus.APPROVED))
                .filter(b -> b.getEndDate().isBefore(currentDateTime))
                .collect(Collectors.toList());
        CommentDto result = null;
        if(bookingList.size() > 0) {
            for (Booking booking : bookingList) {
                if (booking.getStartDate().isAfter(currentDateTime)) {
                    booking.setStatus(BookingStatus.FUTURE);
                } else if (booking.getStartDate().isBefore(currentDateTime) && booking.getEndDate().isAfter(currentDateTime)) {
                    booking.setStatus(BookingStatus.CURRENT);
                }
            }
            if (item.isPresent() && user.isPresent() && !text.isBlank()) {
                Optional<Comment> commentByItemAndUser = commentRepository.findByItemIdAndAuthorId(item.get().getId(),
                        user.get().getId());
                if (commentByItemAndUser.isEmpty()) {
                    Comment comment = new Comment(text, item.get(), user.get());
                    result = commentDtoMapper.commentToDto(commentRepository.save(comment));
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

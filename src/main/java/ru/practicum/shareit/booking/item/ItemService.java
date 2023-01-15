package ru.practicum.shareit.booking.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.item.comments.CommentRepository;
import ru.practicum.shareit.booking.item.comments.dto.CommentDto;
import ru.practicum.shareit.booking.item.comments.dto.CommentDtoMapper;
import ru.practicum.shareit.booking.item.comments.model.Comment;
import ru.practicum.shareit.booking.item.dto.ItemDto;
import ru.practicum.shareit.booking.item.dto.ItemDtoMapper;
import ru.practicum.shareit.booking.item.model.Item;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemDtoMapper itemDtoMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final BookingDtoMapper bookingDtoMapper;
    private final CommentDtoMapper commentDtoMapper;

    public ItemService(ItemRepository itemRepository, ItemDtoMapper itemDtoMapper, UserRepository userRepository,
                       BookingRepository bookingRepository, CommentRepository commentRepository,
                       BookingDtoMapper bookingDtoMapper, CommentDtoMapper commentDtoMapper) {
        this.itemRepository = itemRepository;
        this.itemDtoMapper = itemDtoMapper;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.bookingDtoMapper = bookingDtoMapper;
        this.commentDtoMapper = commentDtoMapper;
    }

    public ItemDto createItem(ItemDto itemDto) {
        Long ownerId = itemDto.getOwnerId();
        Optional<User> user = userRepository.findById(ownerId);
        if (user.isPresent()) {
            return itemDtoMapper.itemToDto(itemRepository.save(itemDtoMapper.dtoToItem(itemDto)));
        } else {
            throw new DataNotFoundException("Пользователь не найден");
        }
    }

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
                addLastAndNextBookingToItem(itemDto, bookingList);
            }
            return itemDto;
        } else {
            throw new DataNotFoundException("Предмет не найден");
        }
    }

    public List<ItemDto> getAllItemsOfUser(Long userId) {
        List<ItemDto> itemDtos = new ArrayList<>();
        List<Booking> bookingList;
        for (Item item : itemRepository.findByOwnerIdOrderById(userId)) {
            ItemDto itemDto = itemDtoMapper.itemToDto(item);
            bookingList = bookingRepository.findByItemIdAndItemOwnerId(item.getId(), userId);
            itemDtos.add(itemDto);
            addLastAndNextBookingToItem(itemDto, bookingList);
        }
        return itemDtos;
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
        if (bookingList.size() > 1) {
            itemDto.setLastBooking(bookingDtoMapper.bookingToDto(bookingList.get(0)));
            bookingList.sort((b1, b2) -> b2.getStartDate().compareTo(b1.getStartDate()));
            itemDto.setNextBooking(bookingDtoMapper.bookingToDto(bookingList.get(0)));
        } else if (bookingList.size() > 0) {
            itemDto.setLastBooking(bookingDtoMapper.bookingToDto(bookingList.get(0)));
        }
    }

    public CommentDto createCommentToItem(Long itemId, String text, Long userId) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Optional<Item> item = itemRepository.findById(itemId);
        Optional<User> user = userRepository.findById(userId);
        List<Booking> bookingList = bookingRepository.findByItemIdAndBookerId(itemId, userId);
        CommentDto result = null;
        for (Booking booking : bookingList) {
            if (booking.getStartDate().isAfter(currentDateTime.plusSeconds(1))) {
                booking.setStatus(BookingStatus.FUTURE);
            } else if (booking.getStartDate().isBefore(currentDateTime.plusSeconds(1)) && booking.getEndDate().isAfter(currentDateTime.plusSeconds(1))) {
                booking.setStatus(BookingStatus.CURRENT);
            }
        }
        for (Booking booking : bookingList) {
            if (item.isPresent() && user.isPresent() && !text.isBlank()) {
                switch (booking.getStatus()) {
                    case WAITING:
                    case REJECTED:
                    case CANCELED:
                    case FUTURE:
                        break;
                    default:
                        Comment comment = new Comment(text, item.get(), user.get());
                        result = commentDtoMapper.commentToDto(commentRepository.save(comment));
                        break;
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

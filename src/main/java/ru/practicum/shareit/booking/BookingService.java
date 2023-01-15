package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.item.ItemRepository;
import ru.practicum.shareit.booking.item.dto.ItemDtoMapper;
import ru.practicum.shareit.booking.item.model.Item;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDtoMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BookingService {
    private final BookingDtoMapper bookingDtoMapper;
    private final ItemDtoMapper itemDtoMapper;
    private final UserDtoMapper userDtoMapper;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    LocalDateTime currentDateTime = LocalDateTime.now().withNano(0);

    public BookingService(BookingDtoMapper bookingDtoMapper, ItemDtoMapper itemDtoMapper,
                          UserDtoMapper userDtoMapper, BookingRepository bookingRepository,
                          ItemRepository itemRepository, UserRepository userRepository) {
        this.bookingDtoMapper = bookingDtoMapper;
        this.itemDtoMapper = itemDtoMapper;
        this.userDtoMapper = userDtoMapper;
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public BookingDto createBooking(BookingDto bookingDto, Long bookerId) {
        Optional<Item> item = itemRepository.findById(bookingDto.getItemId());
        if (userRepository.findById(bookerId).isPresent()
                && item.isPresent() && !item.get().getOwner().getId().equals(bookerId)) {
            bookingDto.setItem(itemDtoMapper.itemToDto(item.get()));
            if (
                    checkItemIsAvailable(bookingDto.getItem().getId())
                            && (bookingDto.getEnd().equals(currentDateTime) ||
                            (bookingDto.getEnd().isAfter(currentDateTime)
                                    && bookingDto.getEnd().isAfter(bookingDto.getStart())
                                    && bookingDto.getStart().isAfter(currentDateTime)))
            ) {
                userRepository.findById(bookerId)
                        .ifPresent(user -> bookingDto.setBooker(userDtoMapper.userToDto(user)));
                bookingDto.setStatus(BookingStatus.WAITING);
                Booking booking = bookingDtoMapper.dtoToBooking(bookingDto);
                return bookingDtoMapper.bookingToDto(bookingRepository.save(booking));
            } else {
                throw new IncorrectDataException("Переданы некорректные данные");
            }
        } else {
            throw new DataNotFoundException("Пользователь не найден");
        }
    }

    public BookingDto updateBooking(Long userId, Long bookingId, boolean approvedType) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isPresent() && booking.get().getItem().getOwner().getId().equals(userId)) {
            if (!booking.get().getStatus().equals(BookingStatus.APPROVED)) {
                if (approvedType) {
                    booking.get().setStatus(BookingStatus.APPROVED);
                } else {
                    booking.get().setStatus(BookingStatus.REJECTED);
                }
                Booking booking1 = bookingRepository.save(booking.get());
                return bookingDtoMapper.bookingToDto(booking1);
            }
            throw new IncorrectDataException("Переданы некорректные данные");
        } else {
            throw new DataNotFoundException("Переданы некорректные данные");
        }
    }

    public BookingDto getBooking(Long userId, Long bookingId) {
        Optional<Booking> bookingOwner = bookingRepository.findById(bookingId);
        Item item;
        if (bookingOwner.isPresent()) {
            item = itemRepository.findById(bookingOwner.get().getItem().getId()).orElse(null);
            Optional<Booking> bookingWithUserId = bookingRepository.findByIdAndBookerId(bookingId, userId);
            if (item != null && item.getOwner().getId().equals(userId)) {
                return bookingDtoMapper.bookingToDto(bookingOwner.get());
            } else if (bookingWithUserId.isPresent()) {
                return bookingDtoMapper.bookingToDto(bookingWithUserId.get());
            } else {
                throw new DataNotFoundException("Данные не найдены");
            }
        } else {
            throw new DataNotFoundException("Данные не найдены");
        }
    }

    public List<BookingDto> getAllBookingsOwner(Long userId, String state) {
        BookingStatus stateFromString = BookingStatus.ALL;
        Optional<User> user = userRepository.findById(userId);
        if (state != null) {
            stateFromString = stateToStatus(state);
        }
        if (user.isPresent()) {
            List<Booking> bookingList = bookingRepository.findByItemOwnerId(userId);
            return getBookingListByState(stateFromString, bookingList);
        } else {
            throw new DataNotFoundException("Пользователь не найден");
        }
    }

    public List<BookingDto> getAllBookings(Long userId, String state) {
        BookingStatus stateFromString = BookingStatus.ALL;
        if (state != null) {
            stateFromString = stateToStatus(state);
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            List<Booking> bookingList = bookingRepository.findAllByBookerIdOrderByIdDesc(userId);
            return getBookingListByState(stateFromString, bookingList);
        } else {
            throw new DataNotFoundException("Пользователь не найден");
        }
    }

    private BookingStatus stateToStatus(String state) {
        BookingStatus stateFromString;
        switch (state) {
            case "WAITING":
                stateFromString = BookingStatus.WAITING;
                break;
            case "CURRENT":
                stateFromString = BookingStatus.CURRENT;
                break;
            case "REJECTED":
                stateFromString = BookingStatus.REJECTED;
                break;
            case "PAST":
                stateFromString = BookingStatus.PAST;
                break;
            case "FUTURE":
                stateFromString = BookingStatus.FUTURE;
                break;
            case "ALL":
                stateFromString = BookingStatus.ALL;
                break;
            default:
                throw new IncorrectDataException("Unknown state: UNSUPPORTED_STATUS");
        }
        return stateFromString;
    }

    private boolean checkItemIsAvailable(Long itemId) {
        return Objects.requireNonNull(itemRepository.findById(itemId).orElse(null)).getAvailable();
    }

    private List<Booking> getBookingsByStatus(List<Booking> bookingList, BookingStatus bookingStatus) {
        List<Booking> result = new ArrayList<>();
        LocalDateTime currentDatetime = LocalDateTime.now();
        switch (bookingStatus) {
            case CURRENT:
                for (Booking booking : bookingList) {
                    if (booking.getStartDate().isBefore(currentDatetime) && booking.getEndDate().isAfter(currentDatetime)) {
                        result.add(booking);
                    }
                }
                break;
            case PAST:
                for (Booking booking : bookingList) {
                    if (booking.getEndDate().isBefore(currentDatetime)) {
                        result.add(booking);
                    }

                }
                break;
            default:
                result.addAll(bookingList);
                break;
        }
        return result;
    }

    private List<BookingDto> getBookingListByState(BookingStatus stateFromString, List<Booking> bookingList) {
        List<BookingDto> result = new ArrayList<>();
        bookingList.sort((b1, b2) -> b2.getId().compareTo(b1.getId()));
        if (stateFromString.equals(BookingStatus.ALL)) {
            for (Booking booking : bookingList) {
                result.add(bookingDtoMapper.bookingToDto(booking));
            }
            return result;
        }
        for (Booking booking : getBookingsByStatus(bookingList, stateFromString)) {
            if (booking.getStatus().equals(stateFromString)
                    || stateFromString.equals(BookingStatus.CURRENT)
                    || stateFromString.equals(BookingStatus.PAST)
                    || stateFromString.equals(BookingStatus.FUTURE)
            ) {
                result.add(bookingDtoMapper.bookingToDto(booking));
            }
        }
        return result;
    }
}

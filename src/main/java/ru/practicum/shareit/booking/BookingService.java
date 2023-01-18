package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingInDto;
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

    public BookingDto createBooking(BookingInDto bookingInDto, Long bookerId) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Optional<Item> item = itemRepository.findById(bookingInDto.getItemId());
        BookingDto bookingDto = bookingDtoMapper.inDtoToDto(bookingInDto);
        List<Booking> itemBookings = bookingRepository.findByItemId(bookingDto.getItemId());
        for (Booking itemBooking : itemBookings) {
            if (
                    (bookingDto.getStart().isAfter(itemBooking.getStartDate())
                            && bookingDto.getStart().isBefore(itemBooking.getEndDate()))
                            ||
                            (bookingDto.getEnd().isAfter(itemBooking.getStartDate())
                                    && bookingDto.getEnd().isBefore(itemBooking.getEndDate()))
            ) {
                throw new IncorrectDataException("Переданы неверные данные");
            }
        }
        if (userRepository.findById(bookerId).isPresent()
                && item.isPresent() && !item.get().getOwner().getId().equals(bookerId)) {
            bookingDto.setItem(itemDtoMapper.itemToDto(item.get()));
            if (
                    checkItemIsAvailable(bookingDto.getItem().getId())
                            && !bookingDto.getEnd().isBefore(bookingDto.getStart())
                            && !bookingDto.getStart().isBefore(currentDateTime)
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
        booking.orElseThrow(() -> new DataNotFoundException("Переданы некорректные данные"));
        if (!booking.get().getItem().getOwner().getId().equals(userId)) {
            throw new DataNotFoundException("Переданы некорректные данные");
        }
        if (!booking.get().getStatus().equals(BookingStatus.WAITING)) {
            throw new IncorrectDataException("Переданы некорректные данные");
        }
        if (approvedType) {
            booking.get().setStatus(BookingStatus.APPROVED);
        } else {
            booking.get().setStatus(BookingStatus.REJECTED);
        }
        Booking booking1 = bookingRepository.save(booking.get());
        return bookingDtoMapper.bookingToDto(booking1);
    }

    public BookingDto getBooking(Long userId, Long bookingId) {
        Optional<Booking> bookingOwner = bookingRepository.findById(bookingId);
        bookingOwner.orElseThrow(() -> new DataNotFoundException("Переданы некорректные данные"));
        Item item;
        item = itemRepository.findById(bookingOwner.get().getItem().getId()).orElse(null);
        if ((item != null && item.getOwner().getId().equals(userId))
                || bookingOwner.get().getBooker().getId().equals(userId)) {
            return bookingDtoMapper.bookingToDto(bookingOwner.get());
        } else {
            throw new DataNotFoundException("Данные не найдены");
        }
    }

    public List<BookingDto> getAllBookingsOwner(Long userId, String state) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        BookingStatus stateFromString;
        stateFromString = stateToStatus(state);
        switch (stateFromString) {
            case FUTURE:
                return getAllBookingsGeneral(
                        bookingRepository.findAllByItemOwnerIdAndStartDateAfterOrderByIdDesc(userId, currentDateTime),
                        userId);
            case PAST:
                return getAllBookingsGeneral(
                        bookingRepository.findAllByItemOwnerIdAndEndDateBeforeOrderByIdDesc(userId, currentDateTime),
                        userId);
            case CURRENT:
                return getAllBookingsGeneral(
                        bookingRepository.findAllByItemOwnerIdAndEndDateAfterAndStartDateBeforeOrderByIdDesc(userId,
                                currentDateTime,
                                currentDateTime),
                        userId);
            case ALL:
                return getAllBookingsGeneral(bookingRepository.findByItemOwnerIdOrderByIdDesc(userId), userId);
            default:
                return getAllBookingsGeneral(bookingRepository.findByItemOwnerIdAndStatusOrderByIdDesc(userId,
                                stateFromString),
                        userId);
        }
    }

    public List<BookingDto> getAllBookings(Long userId, String state) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        BookingStatus stateFromString = BookingStatus.ALL;
        if (state != null) {
            stateFromString = stateToStatus(state);
        }
        switch (stateFromString) {
            case FUTURE:
                return getAllBookingsGeneral(
                        bookingRepository.findAllByBookerIdAndStartDateAfterOrderByIdDesc(userId, currentDateTime),
                        userId);
            case PAST:
                return getAllBookingsGeneral(
                        bookingRepository.findAllByBookerIdAndEndDateBeforeOrderByIdDesc(userId, currentDateTime),
                        userId);
            case CURRENT:
                return getAllBookingsGeneral(
                        bookingRepository.findAllByBookerIdAndEndDateAfterAndStartDateBeforeOrderByIdDesc(userId,
                                currentDateTime,
                                currentDateTime),
                        userId);
            case ALL:
                return getAllBookingsGeneral(bookingRepository.findAllByBookerIdOrderByIdDesc(userId), userId);
            default:
                return getAllBookingsGeneral(bookingRepository.findAllByBookerIdAndStatusOrderByIdDesc(userId,
                                stateFromString),
                        userId);
        }
    }

    private List<BookingDto> getAllBookingsGeneral(List<Booking> bookingList, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        return getBookingListByState(bookingList);
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

    private List<BookingDto> getBookingListByState(List<Booking> bookingList) {
        List<BookingDto> result = new ArrayList<>();
        for (Booking booking : bookingList) {
            result.add(bookingDtoMapper.bookingToDto(booking));
        }
        return result;
    }
}

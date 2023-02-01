package ru.practicum.shareit.booking;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDtoMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
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

    @Transactional
    public BookingDto createBooking(BookingInDto bookingInDto, Long bookerId) {
        LocalDateTime currentDateTime = LocalDateTime.now().withNano(0);
        Item item = itemRepository.findById(bookingInDto.getItemId())
                .orElseThrow(() -> new DataNotFoundException("Предмет не найден"));
        User user = userRepository.findById(bookerId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        BookingDto bookingDto = bookingDtoMapper.inDtoToDto(bookingInDto);
        List<Booking> itemBookings = bookingRepository.findByItemId(bookingDto.getItemId());
        for (Booking itemBooking : itemBookings) {
            if ((!bookingDto.getStart().isBefore(itemBooking.getEndDate()) || !bookingDto.getEnd().isAfter(itemBooking.getStartDate()))) {
                continue;
            }
            throw new IncorrectDataException("Переданы неверные данные");
        }

        if (!item.getOwner().getId().equals(bookerId)) {
            bookingDto.setItem(itemDtoMapper.itemToDto(item));
            if (
                    checkItemIsAvailable(item)
                            && !bookingDto.getEnd().isBefore(bookingDto.getStart())
                            && !bookingDto.getStart().isBefore(currentDateTime)
            ) {
                bookingDto.setBooker(userDtoMapper.userToDto(user));
                bookingDto.setStatus(BookingStatus.WAITING);
                Booking booking = bookingDtoMapper.dtoToBooking(bookingDto, user);
                return bookingDtoMapper.bookingToDto(bookingRepository.save(booking));
            } else {
                throw new IncorrectDataException("Переданы некорректные данные");
            }
        } else {
            throw new DataNotFoundException("Пользователь не найден");
        }
    }

    @Transactional
    public BookingDto updateBooking(Long userId, Long bookingId, boolean approvedType) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Переданы некорректные данные"));
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new DataNotFoundException("Переданы некорректные данные");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new IncorrectDataException("Переданы некорректные данные");
        }
        if (approvedType) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return bookingDtoMapper.bookingToDto(bookingRepository.save(booking));
    }

    public BookingDto getBooking(Long userId, Long bookingId) {
        Booking bookingOwner = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Переданы некорректные данные"));
        Item item = itemRepository.findById(bookingOwner.getItem().getId())
                .orElseThrow(() -> new DataNotFoundException("Переданы некорректные данные"));
        if (item.getOwner().getId().equals(userId)
                || bookingOwner.getBooker().getId().equals(userId)) {
            return bookingDtoMapper.bookingToDto(bookingOwner);
        } else {
            throw new DataNotFoundException("Данные не найдены");
        }
    }

    public List<BookingDto> getAllBookingsOwner(Long userId, String state, Integer from, Integer size) {
        fromAndSizeCheck(from, size);
        LocalDateTime currentDateTime = LocalDateTime.now().withNano(0);
        BookingStatus stateFromString = stateToStatus(state);
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
                List<Booking> bookings = bookingRepository.findAllByItemOwnerIdOrderByIdDesc(userId, PageRequest.of(from, size));
                return getAllBookingsGeneral(bookings, userId);
            default:
                return getAllBookingsGeneral(bookingRepository.findByItemOwnerIdAndStatusOrderByIdDesc(userId,
                                stateFromString),
                        userId);
        }
    }

    public List<BookingDto> getAllBookings(Long userId, String state, Integer from, Integer size) {
        fromAndSizeCheck(from, size);
        LocalDateTime currentDateTime = LocalDateTime.now().withNano(0);
        BookingStatus stateFromString = stateToStatus(state);
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
                List<Booking> bookings = bookingRepository.findAllByOrderByIdDesc(PageRequest.of(from, size));
                return getAllBookingsGeneral(bookings, userId);
            default:
                return getAllBookingsGeneral(bookingRepository.findAllByBookerIdAndStatusOrderByIdDesc(userId,
                                stateFromString),
                        userId);
        }

    }

    private void fromAndSizeCheck(Integer from, Integer size) {
        if (from < 0 || size < 1) {
            throw new IncorrectDataException("Переданы некорректные данные");
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

    private boolean checkItemIsAvailable(Item item) {
        if (item != null) {
            return item.getAvailable();
        }
        return false;
    }

    private List<BookingDto> getBookingListByState(List<Booking> bookingList) {
        List<BookingDto> result = new ArrayList<>();
        for (Booking booking : bookingList) {
            result.add(bookingDtoMapper.bookingToDto(booking));
        }
        return result;
    }
}

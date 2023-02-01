package ru.practicum.shareit.bookings.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserDtoMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingDtoMapper bookingDtoMapper;
    @Mock
    private ItemDtoMapper itemDtoMapper;
    @Mock
    private UserDtoMapper userDtoMapper;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BookingService bookingService;
    LocalDateTime now = LocalDateTime.now().withNano(0);
    LocalDateTime tomorrow = now.plusDays(1);
    LocalDateTime dayAfterTomorrow = tomorrow.plusDays(1);
    User itemOwner = new User(0L, "email@mail.com", "name");
    UserDto userDto = new UserDto(0L, "email@mail.com", "name");
    User booker = new User(1L, "booker@booker.com", "booker");
    Item itemToSave = new Item(0L, "name", "description", true, itemOwner, null);
    ItemDto itemDto = new ItemDto(0L, "name", "description", true, itemOwner.getId(), null, null);
    Booking booking = new Booking(0L, tomorrow.plusDays(2), dayAfterTomorrow.plusDays(2), itemToSave, itemOwner, BookingStatus.APPROVED);
    BookingDto bookingDto = new BookingDto(0L, tomorrow, dayAfterTomorrow, itemToSave.getId(), itemDto, userDto, userDto.getId(), BookingStatus.APPROVED);
    BookingInDto bookingInDto = new BookingInDto(0L, tomorrow, dayAfterTomorrow);
    Long itemId = itemToSave.getId();
    Long bookerId = booker.getId();

    @BeforeEach
    void setup() {
        lenient().when(userDtoMapper.userToDto(itemOwner))
                .thenReturn(new UserDto(itemOwner.getId(), itemOwner.getEmail(), itemOwner.getName()));
        lenient().when(userDtoMapper.dtoToUser(userDto))
                .thenReturn(new User(userDto.getId(), userDto.getEmail(), userDto.getName()));
        lenient().when(itemDtoMapper.itemToDto(itemToSave))
                .thenReturn(new ItemDto(itemId, itemToSave.getName(), itemToSave.getDescription(), itemToSave.getAvailable(), itemToSave.getOwner().getId(), null, null));
        lenient().when(itemDtoMapper.dtoToItem(itemDto, itemOwner))
                .thenReturn(new Item(itemDto.getId(), itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable(), itemOwner, new ItemRequest()));
        lenient().when(bookingDtoMapper.bookingToDto(booking))
                .thenReturn(new BookingDto(booking.getId(), booking.getStartDate(), booking.getEndDate(), itemId, itemDto, userDto, userDto.getId(), booking.getStatus()));
        lenient().when(bookingDtoMapper.dtoToBooking(bookingDto, itemOwner))
                .thenReturn(new Booking(bookingDto.getId(), bookingDto.getStart(), bookingDto.getEnd(), itemToSave, itemOwner, bookingDto.getStatus()));
        lenient().when(bookingDtoMapper.inDtoToDto(bookingInDto))
                .thenReturn(new BookingDto(bookingInDto.getItemId(), bookingInDto.getStart(), bookingInDto.getEnd()));
        lenient().when(userRepository.findById(bookerId)).thenReturn(Optional.of(new User()));
        lenient().when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToSave));
    }

    @Test
    void createBooking_itemFoundAndAvailableAndUserFound_thenCreateBooking() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToSave));
        when(bookingRepository.findByItemId(itemId)).thenReturn(List.of(booking));
        when(bookingDtoMapper.inDtoToDto(bookingInDto)).thenReturn(bookingDto);
        when(bookingDtoMapper.dtoToBooking(bookingDto, new User())).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);


        BookingDto bookingDto1 = bookingService.createBooking(bookingInDto, bookerId);

        assertEquals(new BookingDto(0L, tomorrow.plusDays(2), dayAfterTomorrow.plusDays(2), itemToSave.getId(), itemDto, userDto, userDto.getId(), BookingStatus.APPROVED), bookingDto1);
        verify(bookingRepository).save(booking);
    }

    @Test
    void createBooking_whenItemNotFound_thenThrowException() {
        lenient().when(itemRepository.findById(itemId)).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> bookingService.createBooking(bookingInDto, bookerId));
    }

    @Test
    void createBooking_whenUserNotFound_thenThrowException() {
        when(userRepository.findById(99L)).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> bookingService.createBooking(bookingInDto, 99L));
    }

    @Test
    void createBooking_whenItemIsNotAvailable_thenThrowException() {
        itemToSave.setAvailable(false);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToSave));
        when(bookingDtoMapper.inDtoToDto(bookingInDto)).thenReturn(bookingDto);

        assertThrows(IncorrectDataException.class, () -> bookingService.createBooking(bookingInDto, bookerId));
    }

    @Test
    void createBooking_whenBookingEndBeforeStart_thenThrowException() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToSave));
        bookingDto.setStart(dayAfterTomorrow);
        bookingDto.setEnd(tomorrow);
        when(bookingDtoMapper.inDtoToDto(bookingInDto)).thenReturn(bookingDto);

        assertThrows(IncorrectDataException.class, () -> bookingService.createBooking(bookingInDto, bookerId));
    }

    @Test
    void createBooking_whenBookingStartIsAfterNow_thenThrowException() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToSave));
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        when(bookingDtoMapper.inDtoToDto(bookingInDto)).thenReturn(bookingDto);

        assertThrows(IncorrectDataException.class, () -> bookingService.createBooking(bookingInDto, bookerId));
    }

    @Test
    void updateBooking_whenBookingFound_thenUpdateBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(new Booking(0L, tomorrow.plusDays(2), dayAfterTomorrow.plusDays(2), itemToSave, itemOwner, BookingStatus.WAITING)));
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto bookingDto1 = bookingService.updateBooking(itemOwner.getId(), booking.getId(), true);

        assertEquals(bookingDto, bookingDto1);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void updateBooking_whenBookingNotFound_thenThrowException() {
        when(bookingRepository.findById(booking.getId())).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> bookingService.updateBooking(itemOwner.getId(), booking.getId(), true));
    }

    @Test
    void updateBooking_whenItemOwnerOneEqualsUserId_thenThrowException() {
        assertThrows(DataNotFoundException.class, () -> bookingService.updateBooking(99L, booking.getId(), true));
    }

    @Test
    void updateBooking_whenBookingStatusNotWaiting_thenThrowException() {
        booking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(IncorrectDataException.class, () -> bookingService.updateBooking(itemOwner.getId(), booking.getId(), true));

    }

    @Test
    void getBooking_whenBookingFoundAndUserEqualsOwner_thenReturnBooking() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToSave));
        when(bookingService.getBooking(itemOwner.getId(), booking.getId())).thenReturn(bookingDto);

        BookingDto result = bookingService.getBooking(itemOwner.getId(), booking.getId());

        assertEquals(bookingDto, result);
    }

    @Test
    void getBooking_whenBookingNotFound_thenThrowException() {
        when(bookingRepository.findById(booking.getId())).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> bookingService.getBooking(itemOwner.getId(), booking.getId()));
    }

    @Test
    void getBooking_whenItemNotFound_thenThrowException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(itemRepository.findById(itemId)).thenThrow(DataNotFoundException.class);

        assertThrows(DataNotFoundException.class, () -> bookingService.getBooking(itemOwner.getId(), booking.getId()));
    }

    @Test
    void getBooking_whenUserNotEqualsOwner_thenThrowException() {
        itemToSave.setOwner(new User(99L, "e@mail.ru", "name"));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToSave));

        assertThrows(DataNotFoundException.class, () -> bookingService.getBooking(bookerId, booking.getId()));
    }

    @Test
    void getAllBookingsOwner_whenStatusFuture_thenReturnBooking() {
        Booking newBooking = new Booking(0L, now, now, itemToSave, itemOwner, BookingStatus.APPROVED);
        when(userRepository.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        when(bookingRepository.findAllByItemOwnerIdAndStartDateAfterOrderByIdDesc(itemOwner.getId(), now))
                .thenReturn(List.of(newBooking));

        assertEquals(List.of(bookingDto), bookingService.getAllBookingsOwner(itemOwner.getId(), "FUTURE", 0, 1));
    }

    @Test
    void getAllBookingsOwner_whenStatusAllAndFromSizeIsNull_thenReturnBooking() {
        Booking newBooking = new Booking(0L, now, now, itemToSave, itemOwner, BookingStatus.APPROVED);
        when(userRepository.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        int from = 0;
        int size = 1;
        when(bookingRepository.findAllByItemOwnerIdOrderByIdDesc(itemOwner.getId(), PageRequest.of(from, size)))
                .thenReturn(List.of(newBooking));

        assertEquals(List.of(bookingDto), bookingService.getAllBookingsOwner(itemOwner.getId(), "ALL", from, size));
    }

    @Test
    void getAllBookingsOwner_whenStatusAllAndFromSizeIsNotNull_thenReturnBooking() {
        Booking newBooking = new Booking(0L, now, now, itemToSave, itemOwner, BookingStatus.APPROVED);
        when(userRepository.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        booking.setStartDate(now);
        booking.setEndDate(now);
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        int from = 0;
        int size = 1;
        when(bookingRepository.findAllByItemOwnerIdOrderByIdDesc(itemOwner.getId(), PageRequest.of(from, size)))
                .thenReturn(List.of(newBooking));

        assertEquals(List.of(bookingDto), bookingService.getAllBookingsOwner(itemOwner.getId(), "ALL", from, size));
    }

    @Test
    void getAllBookingsOwner_whenStatusRejected_thenReturnBooking() {
        Booking newBooking = new Booking(0L, now, now, itemToSave, itemOwner, BookingStatus.APPROVED);
        when(userRepository.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByIdDesc(itemOwner.getId(), BookingStatus.REJECTED))
                .thenReturn(List.of(newBooking));

        assertEquals(List.of(bookingDto), bookingService.getAllBookingsOwner(itemOwner.getId(), "REJECTED", 0, 1));
    }

    @Test
    void getAllBookings_whenStatusFuture_thenReturnBooking() {
        Booking newBooking = new Booking(0L, now, now, itemToSave, itemOwner, BookingStatus.APPROVED);
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        when(bookingRepository.findAllByBookerIdAndStartDateAfterOrderByIdDesc(bookerId, now))
                .thenReturn(List.of(newBooking));

        assertEquals(List.of(bookingDto), bookingService.getAllBookings(bookerId, "FUTURE", 0, 1));
    }

    @Test
    void getAllBookings_whenStatusAllAndFromSizeIsNull_thenReturnBooking() {
        Booking newBooking = new Booking(0L, now, now, itemToSave, itemOwner, BookingStatus.APPROVED);
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        int from = 0;
        int size = 1;
        when(bookingRepository.findAllByOrderByIdDesc(PageRequest.of(from, size))).thenReturn(List.of(newBooking));

        assertEquals(List.of(bookingDto), bookingService.getAllBookings(bookerId, "ALL", from, size));
    }

    @Test
    void getAllBookings_whenStatusAllAndFromSizeIsNotNull_thenReturnBooking() {
        int from = 0;
        int size = 1;
        Booking newBooking = new Booking(0L, now, now, itemToSave, itemOwner, BookingStatus.APPROVED);
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        when(bookingRepository.findAllByOrderByIdDesc(PageRequest.of(from, size))).thenReturn(List.of(newBooking));

        assertEquals(List.of(bookingDto), bookingService.getAllBookings(bookerId, "ALL", from, size));
    }

    @Test
    void getAllBookings_whenStatusRejected_thenReturnBooking() {
        LocalDateTime now = LocalDateTime.now();
        Booking newBooking = new Booking(0L, now, now, itemToSave, itemOwner, BookingStatus.APPROVED);
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByIdDesc(bookerId, BookingStatus.REJECTED))
                .thenReturn(List.of(newBooking));

        assertEquals(List.of(bookingDto), bookingService.getAllBookings(bookerId, "REJECTED", 0, 1));
    }
}

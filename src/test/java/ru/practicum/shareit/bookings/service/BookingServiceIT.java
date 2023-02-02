package ru.practicum.shareit.bookings.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.webservices.client.WebServiceClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import ru.practicum.shareit.user.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@WebServiceClientTest(BookingService.class)
class BookingServiceIT {

    @Autowired
    BookingService bookingService;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BookingDtoMapper bookingDtoMapper;
    @MockBean
    private UserDtoMapper userDtoMapper;
    @MockBean
    private ItemDtoMapper itemDtoMapper;
    @MockBean
    private UserService userService;

    private BookingDto bookingDto;
    private BookingInDto bookingInDto;
    private User user1;
    private User user2;
    private Item item1;
    private Item item2;

    private Booking booking;
    private Booking booking2;

    @BeforeEach
    void beforeEach() {
        user1 = new User(1L, "user 1", "user1@email");
        user2 = new User(2L, "user 2", "user2@email");

        item1 = Item.builder().id(1L).name("item1").description("item1 desc").owner(user1).available(true).build();
        item2 = Item.builder().id(2L).name("item2").description("item2 desc").owner(user2).available(true).build();


        bookingInDto = BookingInDto.builder()
                .itemId(item1.getId())
                .start(LocalDateTime.of(2023, 1, 10, 12, 0))
                .end(LocalDateTime.of(2023, 2, 10, 12, 0))
                .build();

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2023, 11, 12, 11, 11))
                .end(LocalDateTime.of(2023, 12, 12, 11, 11))
                .booker(new UserDto(2L, user2.getName(), user2.getEmail()))
                .bookerId(user2.getId())
                .item(itemDtoMapper.itemToDto(item1))
                .status(BookingStatus.WAITING)
                .build();

        booking = Booking.builder()
                .id(1L)
                .startDate(LocalDateTime.of(2023, 1, 10, 12, 0))
                .endDate(LocalDateTime.of(2023, 2, 10, 12, 0))
                .status(BookingStatus.ALL)
                .booker(user1)
                .item(item1)
                .build();

        booking2 = Booking.builder()
                .id(2L)
                .startDate(LocalDateTime.of(2022, 11, 12, 11, 11))
                .endDate(LocalDateTime.of(2022, 12, 12, 11, 11))
                .status(BookingStatus.WAITING)
                .booker(user1)
                .item(item2)
                .build();
    }


    @Test
    void createBooking() {
        when(bookingDtoMapper.inDtoToDto(any(BookingInDto.class))).thenReturn(bookingDto);
        when(bookingDtoMapper.dtoToBooking(any(BookingDto.class), any(User.class))).thenReturn(booking);
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        assertThrows(DataNotFoundException.class,
                () -> bookingService.createBooking(bookingInDto, user1.getId()));

        assertEquals(bookingService.createBooking(bookingInDto, user2.getId()), bookingDto);
    }

    @Test
    void createInvalidBooking() {
        when(userRepository.findById(user1.getId())).thenReturn(Optional.ofNullable(user1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.ofNullable(user2));
        when(bookingDtoMapper.inDtoToDto(any(BookingInDto.class))).thenReturn(bookingDto);

        item2.setAvailable(false);
        when(itemRepository.findById(item2.getId())).thenReturn(Optional.of(item2));
        when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        when(bookingDtoMapper.dtoToBooking(any(BookingDto.class), any(User.class))).thenReturn(booking);
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);

        assertThrows(DataNotFoundException.class, () -> bookingService.createBooking(bookingInDto, user1.getId()));

        BookingInDto bookingInDtoWrong = BookingInDto.builder()
                .itemId(item1.getId())
                .start(LocalDateTime.of(2023, 1, 10, 12, 0))
                .end(LocalDateTime.of(2023, 2, 10, 12, 0))
                .build();

        assertThrows(DataNotFoundException.class, () -> bookingService.createBooking(bookingInDtoWrong, user1.getId()));

        booking.setBooker(user2);
        booking.setItem(item2);
        item2.setAvailable(false);
        bookingInDtoWrong.setItemId(item2.getId());
        assertThrows(IncorrectDataException.class,
                () -> bookingService.createBooking(bookingInDtoWrong, user1.getId()));
    }

    @Test
    void updateBooking() {
        when(userRepository.findById(user1.getId())).thenReturn(Optional.ofNullable(user1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.ofNullable(user2));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);
        when(bookingDtoMapper.inDtoToDto(any(BookingInDto.class))).thenReturn(bookingDto);

        bookingDto.setStatus(BookingStatus.APPROVED);
        Mockito.when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(booking2));

        assertThrows(DataNotFoundException.class,
                () -> bookingService.updateBooking(user2.getId(), booking.getId(), true));

        assertEquals(bookingService.updateBooking(user2.getId(), booking2.getId(), true).getStatus(),
                BookingStatus.APPROVED);
    }

    @Test
    void getBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        when(userDtoMapper.userToDto(any(User.class))).thenReturn(new UserDto());
        when(itemDtoMapper.itemToDto(any(Item.class)))
                .thenReturn(new ItemDto(1L, "name", "description",
                        true, user1.getId(), Collections.emptyList(), null));

        BookingDto bookingDto = bookingService.getBooking(user1.getId(), booking.getId());

        assertNotNull(bookingDto.getBooker());
        assertEquals(bookingDto.getBookerId(), 2L);
        assertEquals(bookingDto.getStatus(), BookingStatus.WAITING);
    }

    @Test
    void getAllBookings() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        List<Booking> bookingList = List.of(booking);
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        when(bookingRepository.findAllByBookerIdAndStartDateAfterOrderByIdDesc(anyLong(),
                any(LocalDateTime.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByBookerIdAndEndDateBeforeOrderByIdDesc(anyLong(),
                any(LocalDateTime.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByBookerIdAndEndDateAfterAndStartDateBeforeOrderByIdDesc(anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByOrderByIdDesc(PageRequest.of(0, 10))).thenReturn(bookingList);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByIdDesc(anyLong(),
                any(BookingStatus.class))).thenReturn(bookingList);

        List<BookingDto> all = bookingService.getAllBookings(1L, "ALL", 0, 10);
        List<BookingDto> pastList = bookingService.getAllBookings(1L, "PAST", 0, 10);
        List<BookingDto> current = bookingService.getAllBookings(1L, "CURRENT", 0, 10);
        List<BookingDto> future = bookingService.getAllBookings(1L, "FUTURE", 0, 10);
        List<BookingDto> waiting = bookingService.getAllBookings(1L, "WAITING", 0, 10);

        assertEquals(all.size(), 1);
        assertEquals(pastList.size(), 1);
        assertEquals(current.size(), 1);
        assertEquals(future.size(), 1);
        assertEquals(waiting.size(), 1);
    }

    @Test
    void getOwnerAllBookings() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(bookingDto);
        List<Booking> bookingList = List.of(booking);
        when(bookingRepository.findAllByItemOwnerIdAndStartDateAfterOrderByIdDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdAndEndDateBeforeOrderByIdDesc(anyLong(),
                any(LocalDateTime.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdAndEndDateAfterAndStartDateBeforeOrderByIdDesc(anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdOrderByIdDesc(anyLong(),
                any(PageRequest.class))).thenReturn(bookingList);
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByIdDesc(anyLong(),
                any(BookingStatus.class))).thenReturn(bookingList);

        List<BookingDto> all = bookingService.getAllBookingsOwner(1L, "ALL", 0, 10);
        List<BookingDto> pastList = bookingService.getAllBookingsOwner(1L, "PAST", 0, 10);
        List<BookingDto> current = bookingService.getAllBookingsOwner(1L, "CURRENT", 0, 10);
        List<BookingDto> future = bookingService.getAllBookingsOwner(1L, "FUTURE", 0, 10);
        List<BookingDto> waiting = bookingService.getAllBookingsOwner(1L, "WAITING", 0, 10);

        assertEquals(all.size(), 1);
        assertEquals(pastList.size(), 1);
        assertEquals(current.size(), 1);
        assertEquals(future.size(), 1);
        assertEquals(waiting.size(), 1);
    }

    @Test
    void bookingDtoMappingTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        when(userService.getUserById(1L)).thenReturn(new UserDto(user1.getId(), user1.getName(), user1.getEmail()));
        when(userService.getUserById(2L)).thenReturn(new UserDto(user2.getId(), user2.getName(), user2.getEmail()));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemDtoMapper.itemToDto(any(Item.class))).thenReturn(new ItemDto(1L, "name", "description",
                true, user1.getId(), Collections.emptyList(), null));
        when(itemDtoMapper.dtoToItem(any(ItemDto.class), any(User.class))).thenReturn(item1);
        when(userDtoMapper.userToDto(any(User.class))).thenReturn(new UserDto(1L, "booker@mai.ru", "booker"));

        Booking booking1 = Booking.builder()
                .id(1L)
                .startDate(LocalDateTime.of(2023, 1, 10, 12, 0))
                .endDate(LocalDateTime.of(2023, 2, 10, 12, 0))
                .item(item1)
                .booker(user2)
                .status(BookingStatus.WAITING)
                .build();

        BookingDto expectedBookingDto = BookingDto.builder()
                .id(1L)
                .booker(new UserDto(1L, "booker@mai.ru", "booker"))
                .itemId(1L)
                .item(new ItemDto(1L, "itemName", "itemDescription", true, 2L, Collections.emptyList(), null))
                .bookerId(2L)
                .start(LocalDateTime.of(2023, 1, 10, 12, 0))
                .end(LocalDateTime.of(2023, 2, 10, 12, 0))
                .status(BookingStatus.WAITING)
                .build();


        BookingDtoMapper bookingMapper1 = new BookingDtoMapper(itemDtoMapper, userDtoMapper);
        Booking resultBooking = bookingMapper1.dtoToBooking(expectedBookingDto, user2);

        assertEquals(expectedBookingDto.getId(), resultBooking.getId());
        assertEquals(expectedBookingDto.getBookerId(), resultBooking.getBooker().getId());
        assertEquals(expectedBookingDto.getItemId(), resultBooking.getItem().getId());
        assertEquals(expectedBookingDto.getStart(), resultBooking.getStartDate());
        assertEquals(expectedBookingDto.getEnd(), resultBooking.getEndDate());
        assertEquals(expectedBookingDto.getStatus(), resultBooking.getStatus());


        BookingDto resultBookingDto = bookingMapper1.bookingToDto(booking1);

        assertEquals(booking1.getId(), resultBookingDto.getId());
        assertEquals(booking1.getBooker().getId(), resultBooking.getBooker().getId());
        assertEquals(booking1.getItem().getId(), resultBooking.getItem().getId());
        assertEquals(booking1.getStartDate(), resultBooking.getStartDate());
        assertEquals(booking1.getEndDate(), resultBooking.getEndDate());
        assertEquals(booking1.getStatus(), resultBooking.getStatus());
    }
}

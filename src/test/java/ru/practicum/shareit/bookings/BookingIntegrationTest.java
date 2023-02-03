package ru.practicum.shareit.bookings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserDtoMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookingIntegrationTest {
    @Autowired
    private BookingController bookingController;
    @Autowired
    private ItemController itemController;
    @Autowired
    private UserController userController;
    @Autowired
    private UserDtoMapper userDtoMapper;
    @Autowired
    private ItemDtoMapper itemDtoMapper;

    private Booking booking;
    private ItemDto itemDto;
    private User booker;
    private UserDto itemOwner;
    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setup() {
        LocalDateTime start = LocalDateTime.now().withNano(0);
        booker = User.builder().id(2L).email("user1@mail.ru").name("name").build();
        itemOwner = UserDto.builder().id(1L).email("e@mail.ru").name("userName").build();
        itemDto = ItemDto.builder().id(1L).name("itemName").description("itemDescription").available(true).ownerId(itemOwner.getId()).comments(List.of(new CommentDto())).build();
        booking = new Booking(1L, start, start.plusHours(1), itemDtoMapper.dtoToItem(itemDto, booker), booker, BookingStatus.APPROVED);
    }

    @Test
    void createBooking() {
        userController.createUser(itemOwner);
        userController.createUser(userDtoMapper.userToDto(booker));
        itemController.createItem(itemOwner.getId(), itemDto);
        LocalDateTime start = LocalDateTime.now().plusHours(1).withNano(0);
        BookingInDto bookingInDto = BookingInDto.builder().itemId(itemDto.getId()).start(start).end(start.plusHours(1)).build();
        BookingDto bookingDto = bookingController.createBooking(userDtoMapper.userToDto(booker).getId(), bookingInDto);

        assertEquals(bookingDto.getId(), bookingController.getBooking(itemOwner.getId(), bookingDto.getId()).getId());
    }

    @Test
    void updateBookingOk() {
        userController.createUser(itemOwner);
        userController.createUser(userDtoMapper.userToDto(booker));
        itemController.createItem(itemOwner.getId(), itemDto);
        LocalDateTime start = LocalDateTime.now().plusHours(1).withNano(0);
        BookingInDto bookingInDto = BookingInDto.builder().itemId(itemDto.getId()).start(start).end(start.plusHours(1)).build();
        BookingDto bookingDto = bookingController.createBooking(userDtoMapper.userToDto(booker).getId(), bookingInDto);

        assertEquals(BookingStatus.APPROVED, bookingController.updateBooking(itemOwner.getId(), bookingDto.getId(), true).getStatus());
    }

    @Test
    void getBooking() {
        userController.createUser(itemOwner);
        userController.createUser(userDtoMapper.userToDto(booker));
        itemController.createItem(itemOwner.getId(), itemDto);
        LocalDateTime start = LocalDateTime.now().plusHours(1).withNano(0);
        BookingInDto bookingInDto = BookingInDto.builder().itemId(itemDto.getId()).start(start).end(start.plusHours(1)).build();
        BookingDto bookingDto = bookingController.createBooking(userDtoMapper.userToDto(booker).getId(), bookingInDto);

        assertEquals(bookingDto.getId(), bookingController.getBooking(itemDto.getId(), bookingDto.getId()).getId());
    }

    @Test
    void getAllBookings() {
        userController.createUser(itemOwner);
        userController.createUser(userDtoMapper.userToDto(booker));
        itemController.createItem(itemOwner.getId(), itemDto);
        LocalDateTime start = LocalDateTime.now().plusHours(1).withNano(0);
        BookingInDto bookingInDto = BookingInDto.builder().itemId(itemDto.getId()).start(start).end(start.plusHours(1)).build();
        BookingInDto bookingInDto1 = BookingInDto.builder().itemId(itemDto.getId()).start(start.plusDays(1)).end(start.plusDays(1).plusHours(1)).build();
        BookingDto bookingDto = bookingController.createBooking(userDtoMapper.userToDto(booker).getId(), bookingInDto);
        bookingDto.getItem().setOwnerId(1L);
        BookingDto bookingDto1 = bookingController.createBooking(userDtoMapper.userToDto(booker).getId(), bookingInDto1);
        bookingDto1.getItem().setOwnerId(1L);

        assertEquals(List.of(bookingDto1, bookingDto), bookingController.getAllBookings(booker.getId(), "ALL", 0, 10));
    }


    @Test
    void getAllBookingsOwner() {
        userController.createUser(itemOwner);
        userController.createUser(userDtoMapper.userToDto(booker));
        itemController.createItem(itemOwner.getId(), itemDto);
        LocalDateTime start = LocalDateTime.now().plusHours(1).withNano(0);
        BookingInDto bookingInDto = BookingInDto.builder().itemId(itemDto.getId()).start(start).end(start.plusHours(1)).build();
        BookingInDto bookingInDto1 = BookingInDto.builder().itemId(itemDto.getId()).start(start.plusDays(1)).end(start.plusDays(1).plusHours(1)).build();
        BookingDto bookingDto = bookingController.createBooking(booker.getId(), bookingInDto);
        bookingDto.getItem().setOwnerId(1L);
        BookingDto bookingDto1 = bookingController.createBooking(booker.getId(), bookingInDto1);
        bookingDto1.getItem().setOwnerId(1L);

        assertEquals(List.of(bookingDto1, bookingDto), bookingController.getAllBookingsOwner(itemOwner.getId(), "ALL", 0, 10));
    }

    @AfterEach
    void deleteAll() {
        bookingRepository.deleteAll();
    }
}

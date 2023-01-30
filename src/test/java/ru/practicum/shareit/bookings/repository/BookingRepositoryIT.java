package ru.practicum.shareit.bookings.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DataJpaTest
public class BookingRepositoryIT {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    private final LocalDateTime now = LocalDateTime.now().withNano(0);

    private User user = new User(null, "e@mail.ru", "name");
    private ItemRequest itemRequest = new ItemRequest("description", user);
    private Item item =  new Item(null, "name", "description", true, user, itemRequest);
    private Booking booking = new Booking(null, now, now.plusDays(1), item, user, BookingStatus.APPROVED);
    private Long bookerId;
    @BeforeEach
    void setup() {
        user = userRepository.save(user);
        itemRequest = itemRequestRepository.save(itemRequest);
        item = itemRepository.save(item);
        booking = bookingRepository.save(booking);
        bookerId = user.getId();
    }

    @Test
    void findAllByBookerIdOrderByIdDesc() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdOrderByIdDesc(bookerId);

        assertEquals(bookings, List.of(booking));
    }

    @Test
    void findAllByBookerIdAndStatusOrderByIdDesc() {
        BookingStatus status = BookingStatus.APPROVED;

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStatusOrderByIdDesc(bookerId, status);

        assertEquals(bookings, List.of(booking));
    }

    @Test
    void findAllByBookerIdAndStartDateAfterOrderByIdDesc() {
        LocalDateTime tomorrow = now.plusDays(1);
        Booking customBooking = new Booking(null, tomorrow, tomorrow.plusDays(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(customBooking);
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartDateAfterOrderByIdDesc(bookerId, now);

        assertEquals(bookings, List.of(customBooking));
    }

    @Test
    void findAllByBookerIdAndEndDateBeforeOrderByIdDesc() {
        LocalDateTime yesterday = now.minusDays(1);
        Booking customBooking = new Booking(null, yesterday.minusDays(1), yesterday, item, user, BookingStatus.APPROVED);
        bookingRepository.save(customBooking);
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndEndDateBeforeOrderByIdDesc(bookerId, now);

        assertEquals(bookings, List.of(customBooking));
    }

    @Test
    void findAllByBookerIdAndEndDateAfterAndStartDateBeforeOrderByIdDesc() {
        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndEndDateAfterAndStartDateBeforeOrderByIdDesc(bookerId, now, now.plusDays(1));

        assertEquals(bookings, List.of(booking));
    }

    @Test
    void findAllByBookerIdAndStartDateAfterOrderByIdDes() {
        LocalDateTime tomorrow = now.plusDays(1);
        Booking customBooking = new Booking(null, tomorrow, tomorrow.plusDays(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(customBooking);
        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdAndStartDateAfterOrderByIdDesc(bookerId, now);

        assertEquals(bookings, List.of(customBooking));
    }

    @Test
    void findAllByItemOwnerIdAndEndDateBeforeOrderByIdDesc() {
        Booking customBooking = new Booking(null, now.minusDays(1), now, item, user, BookingStatus.APPROVED);
        bookingRepository.save(customBooking);
        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdAndEndDateBeforeOrderByIdDesc(bookerId, now.plusDays(1));

        assertEquals(bookings, List.of(customBooking));
    }

    @Test
    void findAllByItemOwnerIdAndEndDateAfterAndStartDateBeforeOrderByIdDesc() {
        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdAndEndDateAfterAndStartDateBeforeOrderByIdDesc(bookerId, now, now.plusDays(1));

        assertEquals(bookings, List.of(booking));
    }

    @Test
    void findByItemIdAndItemOwnerId() {
        List<Booking> bookings = bookingRepository.findByItemIdAndItemOwnerId(bookerId, item.getId());

        assertEquals(bookings, List.of(booking));
    }

    @Test
    void findByItemIdAndBookerId() {
        List<Booking> bookings = bookingRepository.findByItemIdAndBookerId(bookerId, item.getId());

        assertEquals(bookings, List.of(booking));
    }

    @Test
    void findByItemOwnerIdOrderByIdDesc() {
        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByIdDesc(bookerId);

        assertEquals(bookings, List.of(booking));
    }

    @Test
    void findByItemOwnerIdAndStatusOrderByIdDesc() {
        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByIdDesc(bookerId, BookingStatus.APPROVED);

        assertEquals(bookings, List.of(booking));
    }

    @Test
    void findByItemIdIn() {
        List<Booking> bookings = bookingRepository.findByItemIdIn(List.of(item.getId()));

        assertEquals(bookings, List.of(booking));
    }

    @Test
    void findByItemId() {
        List<Booking> bookings = bookingRepository.findByItemId(item.getId());

        assertEquals(bookings, List.of(booking));
    }

    @Test
    void findAllByOrderByIdDesc() {
        List<Booking> bookings = bookingRepository.findAllByOrderByIdDesc(PageRequest.of(0, 1));

        assertEquals(bookings, List.of(booking));
    }

    @Test
    void findAllByItemOwnerIdOrderByIdDesc() {
        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdOrderByIdDesc(bookerId, PageRequest.of(0, 1));

        assertEquals(bookings, List.of(booking));
    }

    @AfterEach
    void deleteAll() {
        userRepository.deleteAll();
        itemRequestRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
    }
}

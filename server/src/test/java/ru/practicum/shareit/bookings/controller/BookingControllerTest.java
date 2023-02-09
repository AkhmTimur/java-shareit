package ru.practicum.shareit.bookings.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {
    @Mock
    private BookingService bookingService;
    @InjectMocks
    private BookingController bookingController;

    Long userId = 0L;
    Long bookingId = 0L;

    @Test
    void createBooking() {
        BookingInDto bookingInDto = new BookingInDto();
        when(bookingService.createBooking(bookingInDto, userId)).thenReturn(new BookingDto());

        BookingDto actualBooking = bookingController.createBooking(userId, bookingInDto);

        assertEquals(new BookingDto(), actualBooking);
    }

    @Test
    void updateBooking() {
        boolean approvedType = true;
        when(bookingService.updateBooking(userId, bookingId, approvedType)).thenReturn(new BookingDto());

        BookingDto actualBooking = bookingController.updateBooking(userId, bookingId, approvedType);

        assertEquals(new BookingDto(), actualBooking);
    }

    @Test
    void getBooking() {
        when(bookingService.getBooking(userId, bookingId)).thenReturn(new BookingDto());

        BookingDto actualBooking = bookingController.getBooking(userId, bookingId);

        assertEquals(new BookingDto(), actualBooking);
    }

    @Test
    void getAllBookings() {
        String bookingState = "ALL";
        Integer from = 0;
        Integer size = 1;
        when(bookingService.getAllBookings(userId, bookingState, from, size)).thenReturn(List.of(new BookingDto()));

        List<BookingDto> bookingDtoList = bookingController.getAllBookings(userId, bookingState, from, size);

        assertEquals(List.of(new BookingDto()), bookingDtoList);
    }

    @Test
    void getAllBookingsOwner() {
        String bookingState = "ALL";
        Integer from = 0;
        Integer size = 1;
        when(bookingService.getAllBookingsOwner(userId, bookingState, from, size)).thenReturn(List.of(new BookingDto()));

        List<BookingDto> bookingDtoList = bookingController.getAllBookingsOwner(userId, bookingState, from, size);

        assertEquals(List.of(new BookingDto()), bookingDtoList);
    }
}

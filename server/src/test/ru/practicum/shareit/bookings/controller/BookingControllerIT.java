package ru.practicum.shareit.bookings.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInDto;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerIT {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private BookingRepository bookingRepository;

    @PostConstruct
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    @Test
    void createBooking() {
        Long userId = 0L;
        BookingInDto bookingInDto = new BookingInDto();
        BookingDto bookingDto = new BookingDto();
        when(bookingService.createBooking(any(BookingInDto.class), anyLong())).thenReturn(new BookingDto());

        String result = mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingInDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingDto), result);
    }

    @SneakyThrows
    @Test
    void updateBooking() {
        Long userId = 0L;
        Long bookingId = 0L;
        boolean approvedType = true;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(new Booking()));
        when(bookingService.updateBooking(userId, bookingId, approvedType)).thenReturn(new BookingDto());

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", objectMapper.writeValueAsString(approvedType)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService).updateBooking(userId, bookingId, approvedType);
    }

    @SneakyThrows
    @Test
    void getBooking() {
        Long userId = 0L;
        Long bookingId = 0L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(new Booking()));
        when(bookingService.getBooking(userId, bookingId)).thenReturn(new BookingDto());

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService).getBooking(userId, bookingId);
    }

    @SneakyThrows
    @Test
    void getAllBookings() {
        Long userId = 0L;
        String state = "ALL";
        Integer from = 0;
        Integer size = 1;
        when(bookingService.getAllBookings(userId, state, from, size)).thenReturn(List.of(new BookingDto()));

        String result = mockMvc.perform(get("/bookings")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", objectMapper.writeValueAsString(from))
                        .param("size", objectMapper.writeValueAsString(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService).getAllBookings(userId, state, from, size);
    }

    @SneakyThrows
    @Test
    void getAllBookingsOwner() {
        Long userId = 0L;
        String state = "ALL";
        Integer from = 0;
        Integer size = 1;
        when(bookingService.getAllBookingsOwner(userId, state, from, size)).thenReturn(List.of(new BookingDto()));

        String result = mockMvc.perform(get("/bookings/owner")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", objectMapper.writeValueAsString(from))
                        .param("size", objectMapper.writeValueAsString(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService).getAllBookingsOwner(userId, state, from, size);
    }
}

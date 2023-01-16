package ru.practicum.shareit.booking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingInDto {
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}
package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingInDto {
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;


}

package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TODO Sprint add-bookings.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
    private ItemDto item;
    private UserDto booker;
    private Long bookerId;
    private BookingStatus status;

    public BookingDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingDto)) return false;
        BookingDto that = (BookingDto) o;
        return Objects.equals(id, that.id) && Objects.equals(start, that.start) && Objects.equals(end, that.end) && Objects.equals(itemId, that.itemId) && Objects.equals(item, that.item) && Objects.equals(booker, that.booker) && Objects.equals(bookerId, that.bookerId) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, start, end, itemId, item, booker, bookerId, status);
    }
}

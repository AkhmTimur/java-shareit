package ru.practicum.shareit.bookings.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoTest {
    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void jsonTest() throws IOException {
        LocalDateTime dateTime = LocalDateTime.of(2022, 2, 2, 0, 0, 0);
        CommentDto commentDto = new CommentDto(2L, "text", "userName", LocalDate.of(2022, 2, 2));
        ItemDto itemDto = new ItemDto(4L, "name", "description", true, 0L, List.of(commentDto), 0L);
        UserDto userDto = new UserDto(0L, "email@mail.com", "name");
        BookingDto bookingDto = new BookingDto(0L, dateTime, dateTime, itemDto.getId(), itemDto, userDto, userDto.getId(), BookingStatus.APPROVED);

        JsonContent<BookingDto> result = json.write(bookingDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(0);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2022-02-02T00:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2022-02-02T00:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(4);

        assertThat(result).extractingJsonPathMapValue("$.item").extractingByKey("id").isEqualTo(4);
        assertThat(result).extractingJsonPathMapValue("$.item").extractingByKey("name").isEqualTo("name");
        assertThat(result).extractingJsonPathMapValue("$.item").extractingByKey("description").isEqualTo("description");
        assertThat(result).extractingJsonPathMapValue("$.item").extractingByKey("available").isEqualTo(true);
        assertThat(result).extractingJsonPathMapValue("$.item.comments[0]").extractingByKey("id").isEqualTo(2);
        assertThat(result).extractingJsonPathMapValue("$.item.comments[0]").extractingByKey("text").isEqualTo("text");
        assertThat(result).extractingJsonPathMapValue("$.item.comments[0]").extractingByKey("authorName").isEqualTo("userName");
        assertThat(result).extractingJsonPathMapValue("$.item.comments[0]").extractingByKey("created").isEqualTo("2022-02-02");
        assertThat(result).extractingJsonPathMapValue("$.item").extractingByKey("requestId").isEqualTo(0);

        assertThat(result).extractingJsonPathMapValue("$.booker").extractingByKey("id").isEqualTo(0);
        assertThat(result).extractingJsonPathMapValue("$.booker").extractingByKey("name").isEqualTo("name");
        assertThat(result).extractingJsonPathMapValue("$.booker").extractingByKey("email").isEqualTo("email@mail.com");

        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(0);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }
}

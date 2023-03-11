package ru.practicum.shareit.request.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void jsonTest() throws IOException {
        CommentDto commentDto = new CommentDto(2L, "text", "userName", LocalDate.of(2022, 2, 2));
        ItemDto itemDto = new ItemDto(1L, "name", "description", true, 0L, List.of(commentDto), 0L);
        List<ItemDto> items = List.of(itemDto);
        LocalDateTime dateTime = LocalDateTime.of(2022, 2, 2, 0, 0, 0);
        ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "description", dateTime, items);

        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2022-02-02T00:00:00");
        assertThat(result).hasJsonPathArrayValue("$.items")
                .extractingJsonPathMapValue("$.items[0]").extractingByKey("id").isEqualTo(1);
        assertThat(result).hasJsonPathArrayValue("$.items")
                .extractingJsonPathMapValue("$.items[0]").extractingByKey("name").isEqualTo("name");
        assertThat(result).hasJsonPathArrayValue("$.items")
                .extractingJsonPathMapValue("$.items[0]").extractingByKey("description").isEqualTo("description");
        assertThat(result).hasJsonPathArrayValue("$.items")
                .extractingJsonPathMapValue("$.items[0]").extractingByKey("available").isEqualTo(true);
        assertThat(result).hasJsonPathArrayValue("$.items")
                .hasJsonPathMapValue("$.items[0]")
                .hasJsonPathArrayValue("$.items[0].comments")
                .extractingJsonPathMapValue("$.items[0].comments[0]").extractingByKey("id").isEqualTo(2);
        assertThat(result).hasJsonPathArrayValue("$.items")
                .hasJsonPathMapValue("$.items[0]")
                .hasJsonPathArrayValue("$.items[0].comments")
                .extractingJsonPathMapValue("$.items[0].comments[0]").extractingByKey("text").isEqualTo("text");
        assertThat(result).hasJsonPathArrayValue("$.items")
                .hasJsonPathMapValue("$.items[0]")
                .hasJsonPathArrayValue("$.items[0].comments")
                .extractingJsonPathMapValue("$.items[0].comments[0]").extractingByKey("authorName").isEqualTo("userName");
        assertThat(result).hasJsonPathArrayValue("$.items")
                .hasJsonPathMapValue("$.items[0]")
                .hasJsonPathArrayValue("$.items[0].comments")
                .extractingJsonPathMapValue("$.items[0].comments[0]").extractingByKey("created").isEqualTo("2022-02-02");

    }
}

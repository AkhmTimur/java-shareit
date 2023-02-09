package ru.practicum.shareit.item.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoTest {
    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void jsonTest() throws IOException {
        CommentDto commentDto = new CommentDto(2L, "text", "userName", LocalDate.of(2022, 2, 2));
        ItemDto itemDto = new ItemDto(1L, "name", "description", true, 0L, List.of(commentDto), 0L);

        JsonContent<ItemDto> result = json.write(itemDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("name");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathMapValue("$.comments[0]").extractingByKey("id").isEqualTo(2);
        assertThat(result).extractingJsonPathMapValue("$.comments[0]").extractingByKey("text").isEqualTo("text");
        assertThat(result).extractingJsonPathMapValue("$.comments[0]").extractingByKey("authorName").isEqualTo("userName");
        assertThat(result).extractingJsonPathMapValue("$.comments[0]").extractingByKey("created").isEqualTo("2022-02-02");
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(0);
    }
}

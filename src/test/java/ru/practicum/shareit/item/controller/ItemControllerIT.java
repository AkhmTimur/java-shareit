package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerIT {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;

    @SneakyThrows
    @Test
    void createItem() {
        Long userId = 0L;
        ItemDto itemDto = new ItemDto(0L, "name", "description", true, userId, null, null);
        when(itemService.createItem(itemDto)).thenReturn(itemDto);

        String result = mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        assertEquals(objectMapper.writeValueAsString(itemDto), result);
        verify(itemService).createItem(itemDto);
    }

    @SneakyThrows
    @Test
    void updateUser() {
        long itemId = 0L;
        long userId = 0L;
        ItemDto itemDto = new ItemDto(0L, "name", "description", true, userId, null, null);
        when(itemService.updateItem(itemDto)).thenReturn(itemDto);

        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemDto), result);
        verify(itemService).updateItem(itemDto);
    }

    @SneakyThrows
    @Test
    void getItemById() {
        long itemId = 0L;
        long userId = 0L;
        when(itemService.getItemById(itemId, userId)).thenReturn(new ItemDto());

        String result = mockMvc.perform(get("/items/{itemId}", itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(new ItemDto()), result);
        verify(itemService).getItemById(itemId, userId);
    }

    @SneakyThrows
    @Test
    void getAllItemsOfUser() {
        long userId = 0L;
        when(itemService.getAllItemsOfUser(userId)).thenReturn(List.of(new ItemDto()));

        String result = mockMvc.perform(get("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(List.of(new ItemDto())), result);
        verify(itemService).getAllItemsOfUser(userId);
    }

    @SneakyThrows
    @Test
    void searchForItem() {
        String text = "search";
        when(itemService.searchForItem(text)).thenReturn(List.of(new ItemDto()));

        String result = mockMvc.perform(get("/items/search")
                        .contentType("application/json")
                        .param("text", text))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(List.of(new ItemDto())), result);
        verify(itemService).searchForItem(text);
    }

    @SneakyThrows
    @Test
    void createCommentToItem() {
        long userId = 0L;
        long itemId = 0L;
        CommentDto commentDto = new CommentDto();
        String text = "comment";
        commentDto.setText(text);
        when(itemService.createCommentToItem(itemId, text, userId)).thenReturn(new CommentDto());

        String result = mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(new CommentDto()), result);
        verify(itemService).createCommentToItem(itemId, commentDto.getText(), userId);
    }
}

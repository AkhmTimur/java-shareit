package ru.practicum.shareit.item.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {
    @Mock
    private ItemService itemService;
    @InjectMocks
    private ItemController itemController;

    @Test
    void createItem() {
        ItemDto expectedItem = new ItemDto();
        when(itemService.createItem(any())).thenReturn(expectedItem);

        ItemDto itemDto = itemController.createItem(0L, new ItemDto());

        assertEquals(expectedItem, itemDto);
    }

    @Test
    void updateUser() {
        ItemDto expectedItem = new ItemDto();
        when(itemService.updateItem(any())).thenReturn(expectedItem);

        ItemDto itemDto = itemController.updateItem(0L, 0L, new ItemDto());

        assertEquals(expectedItem, itemDto);
    }

    @Test
    void getItemById() {
        ItemDto expectedItem = new ItemDto();
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(expectedItem);

        ItemDto itemDto = itemController.getItemById(0L, 0L);

        assertEquals(expectedItem, itemDto);
    }

    @Test
    void getAllItemsOfUser() {
        List<ItemDto> expectedItems = Collections.emptyList();
        when(itemService.getAllItemsOfUser(anyLong())).thenReturn(expectedItems);

        List<ItemDto> itemDtos = itemController.getAllItemsOfUser(0L);

        assertEquals(expectedItems, itemDtos);
    }

    @Test
    void searchForItem() {
        List<ItemDto> expectedItems = Collections.emptyList();
        when(itemService.searchForItem(anyString())).thenReturn(expectedItems);

        List<ItemDto> itemDtos = itemController.searchForItem("search");

        assertEquals(expectedItems, itemDtos);
    }

    @Test
    void createCommentToItem() {
        CommentDto expectedComment = new CommentDto();
        when(itemService.createCommentToItem(0L, "comment", 0L)).thenReturn(expectedComment);

        CommentDto commentDto = itemController.createCommentToItem(0L, 0L, new CommentDto(0L, "comment", "name", null));

        assertEquals(expectedComment, commentDto);
    }
}

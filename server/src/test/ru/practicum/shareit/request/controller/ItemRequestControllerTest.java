package ru.practicum.shareit.request.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemRequestControllerTest {
    @Mock
    private ItemRequestService itemRequestService;
    @InjectMocks
    private ItemRequestController itemRequestController;

    @Test
    void createItemRequest() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        when(itemRequestService.createItemRequest(anyLong(), any())).thenReturn(itemRequestDto);

        ItemRequestDto itemRequest = itemRequestController.createItemRequest(0L, itemRequestDto);

        assertEquals(itemRequestDto, itemRequest);
    }

    @Test
    void getItemRequests() {
        Long userId = 0L;

        List<ItemRequestDto> itemRequestDtos = itemRequestController.getItemRequests(0L);

        assertEquals(Collections.emptyList(), itemRequestDtos);
    }

    @Test
    void getAllItemRequest() {
        Long userId = 0L;

        List<ItemRequestDto> itemRequestDtos = itemRequestController.getAllItemRequest(userId, null, null);

        assertEquals(Collections.emptyList(), itemRequestDtos);
    }

    @Test
    void getItemRequest() {
        Long userId = 0L;
        Long itemRequestId = 0L;

        ItemRequestDto itemRequestDto = itemRequestController.getItemRequest(userId, itemRequestId);

        assertNull(itemRequestDto);
    }


}

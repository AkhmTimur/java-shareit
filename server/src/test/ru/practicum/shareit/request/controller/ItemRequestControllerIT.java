package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerIT {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @PostConstruct
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    @Test
    void createItemRequest() {
        Long userId = 0L;
        ItemRequestDto itemRequestDto = new ItemRequestDto(0L, "description", LocalDateTime.now().withNano(0));
        when(itemRequestService.createItemRequest(userId, itemRequestDto)).thenReturn(itemRequestDto);

        String result = mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ItemRequestDto itemRequestDto1 = objectMapper.readValue(result, ItemRequestDto.class);
        assertEquals(itemRequestDto.getId(), itemRequestDto1.getId());
        assertEquals(itemRequestDto.getDescription(), itemRequestDto1.getDescription());
        assertEquals(itemRequestDto.getItems(), itemRequestDto1.getItems());
    }

    @SneakyThrows
    @Test
    void getItemRequests() {
        Long userId = 0L;
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        when(itemRequestService.getItemRequests(userId)).thenReturn(List.of(itemRequestDto));

        String result = mockMvc.perform(get("/requests")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(List.of(itemRequestDto)), result);
        verify(itemRequestService).getItemRequests(userId);
    }

    @SneakyThrows
    @Test
    void getAllItemRequestWithFromAndSize() {
        Long userId = 0L;
        Integer from = 0;
        Integer size = 1;
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        when(itemRequestService.getAllItemRequest(userId, from, size)).thenReturn(List.of(itemRequestDto));

        String result = mockMvc.perform(get("/requests/all")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", objectMapper.writeValueAsString(from))
                        .param("size", objectMapper.writeValueAsString(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(List.of(itemRequestDto)), result);
        verify(itemRequestService).getAllItemRequest(userId, from, size);
    }

    @SneakyThrows
    @Test
    void getItemRequest() {
        Long userId = 0L;
        Long requestId = 0L;
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        when(itemRequestService.getItemRequest(userId, requestId)).thenReturn(itemRequestDto);

        String result = mockMvc.perform(get("/requests/{requestId}", requestId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemRequestDto), result);
        verify(itemRequestService).getItemRequest(userId, requestId);
    }
}

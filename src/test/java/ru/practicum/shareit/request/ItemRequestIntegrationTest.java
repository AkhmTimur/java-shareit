package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
public class ItemRequestIntegrationTest {
    @Autowired
    private UserController userController;
    @Autowired
    private ItemRequestController itemRequestController;
    ItemRequestDto itemRequest;
    UserDto userDto;

    @BeforeEach
    void setup() {
        userDto = UserDto.builder().id(1L).email("e@mail.ru").name("name").build();
        itemRequest = ItemRequestDto.builder().id(1L).description("description").created(LocalDateTime.now().withNano(0)).build();
    }

    @Test
    void createItemRequestTest() {
        userDto = userController.createUser(userDto).getBody();
        ItemRequestDto result = itemRequestController.createItemRequest(Objects.requireNonNull(userDto).getId(), itemRequest);

        assertEquals(result.getId(), itemRequestController.getItemRequest(userDto.getId(), result.getId()).getId());

        assertThrows(DataNotFoundException.class, () -> itemRequestController.createItemRequest(99L, itemRequest));
    }

    @Test
    void getItemRequestsTest() {
        userDto = userController.createUser(userDto).getBody();
        ItemRequestDto result = itemRequestController.createItemRequest(Objects.requireNonNull(userDto).getId(), itemRequest);

        List<ItemRequestDto> itemRequestDtos = itemRequestController.getItemRequests(userDto.getId());

        assertEquals(1, itemRequestDtos.size());
        assertEquals(result.getId(), itemRequestDtos.get(0).getId());

        assertThrows(DataNotFoundException.class, () -> itemRequestController.getItemRequests(99L));
    }

    @Test
    void getAllItemRequest() {
        userDto = userController.createUser(userDto).getBody();
        UserDto userDto1 = UserDto.builder().id(2L).email("user@mail.ru").name("userName").build();
        userDto1 = userController.createUser(userDto1).getBody();
        ItemRequestDto result = itemRequestController.createItemRequest(Objects.requireNonNull(userDto1).getId(), itemRequest);

        List<ItemRequestDto> itemRequestDtos = itemRequestController.getAllItemRequest(userDto.getId(), 0, 10);

        assertEquals(1, itemRequestDtos.size());
        assertEquals(result.getId(), itemRequestDtos.get(0).getId());

        assertThrows(DataNotFoundException.class, () -> itemRequestController.getItemRequests(99L));
    }

    @Test
    void getItemRequest() {
        userDto = userController.createUser(userDto).getBody();
        ItemRequestDto result = itemRequestController.createItemRequest(Objects.requireNonNull(userDto).getId(), itemRequest);

        ItemRequestDto itemRequestDto = itemRequestController.getItemRequest(userDto.getId(), result.getId());

        assertEquals(result.getId(), itemRequestDto.getId());

        assertThrows(DataNotFoundException.class, () -> itemRequestController.getItemRequest(99L, itemRequest.getId()));
        assertThrows(DataNotFoundException.class, () -> itemRequestController.getItemRequest(userDto.getId(), 99L));
    }
}

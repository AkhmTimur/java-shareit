package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.user.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerIT {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;

    @SneakyThrows
    @Test
    void getUserById_whenUserNotFound_thenReturnNotFoundStatus() {
        long userId = 0L;
        when(userService.getUserById(userId)).thenThrow(DataNotFoundException.class);

        mockMvc.perform(get("/user/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, never()).getUserById(userId);
    }

    @SneakyThrows
    @Test
    void createUser() {
        UserDto userDto = new UserDto(0L, "email@mail.ru", "name");
        when(userService.createUser(userDto)).thenReturn(userDto);

        String result = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userDto), result);
    }

    @SneakyThrows
    @Test
    void updateUser() {
        Long userId = 0L;
        UserDto userDto = new UserDto(0L, "email@mail.ru", "name");
        when(userRepository.findById(any())).thenReturn(Optional.of(new User()));
        when(userService.updateUser(userDto)).thenReturn(userDto);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        verify(userService).updateUser(userDto);
    }

    @Test
    void deleteUserById() throws Exception {
        Long userId = 0L;

        mockMvc.perform(delete("/users/{userId}", userId)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        verify(userService).deleteUserById(userId);
    }

    @SneakyThrows
    @Test
    void getAllUsers_whenUsersNotFound_thenReturnEmptyList() {
        List<UserDto> userDtoList = Collections.emptyList();

        String users = mockMvc.perform(get("/users")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userDtoList), users);
    }
}

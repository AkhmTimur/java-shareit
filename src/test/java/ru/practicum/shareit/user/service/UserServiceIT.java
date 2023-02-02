package ru.practicum.shareit.user.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserService.class)
public class UserServiceIT {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    private UserRepository userRepository;
    private UserDtoMapper userDtoMapper;

    User user;
    User updatedUser;
    UserDto userDto;
    UserDto expectedUserDto;

    public UserServiceIT(UserRepository userRepository, UserDtoMapper userDtoMapper) {
        this.userRepository = userRepository;
        this.userDtoMapper = userDtoMapper;
    }

    @BeforeEach
    void setup() {
        user = User.builder().id(1L).name("name").email("e@mail.com").build();
        updatedUser = User.builder().id(1L).name("update").email("e@mail.com").build();
        userDto = UserDto.builder().id(1L).name("name").email("e@mail.com").build();
        expectedUserDto = UserDto.builder().id(1L).name("update").email("e@mail.com").build();
    }

    @SneakyThrows
    @Test
    void createUser() {
        String result = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userDto), result);
    }

    @Test
    void getUsers() {
        User user = User.builder().id(0L).name("name1").email("e@mail.com").build();
        User user1 = User.builder().id(0L).name("name1").email("e@mail.com").build();
        UserDto userDto = UserDto.builder().id(0L).name("name1").email("e@mail.com").build();
        UserDto userDto1 = UserDto.builder().id(1L).name("nam2").email("e@mail.com").build();
        List<User> groupOfUsers = List.of(user, user1);
        when(userRepository.findAll()).thenReturn(groupOfUsers);

        List<UserDto> expectedGroupOfUserDtos = List.of(userDto, userDto1);

    }

    @Test
    void updateTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);


    }
}

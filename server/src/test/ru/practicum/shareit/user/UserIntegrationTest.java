package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.DataNotFoundException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserController userController;


    private UserDto userDto;

    @BeforeEach
    void setup() {
        userDto = UserDto.builder().id(1L).name("user1").email("user1@mail.com").build();
    }

    @Test
    void createUserAndGetUserByIdTest() {
        userDto = userController.createUser(userDto).getBody();

        assertEquals(userDto, userService.getUserById(Objects.requireNonNull(userDto).getId()));
    }

    @Test
    void updateUserTest() {
        userDto = userController.createUser(userDto).getBody();
        assertEquals("user1", userService.getUserById(Objects.requireNonNull(userDto).getId()).getName());
        userDto.setName("update");

        userController.updateUser(userDto.getId(), userDto);
        assertEquals("update", userService.getUserById(userDto.getId()).getName());

        UserDto notFoundUser = UserDto.builder().id(99L).name("name").build();
        assertThrows(DataNotFoundException.class, () -> userController.updateUser(notFoundUser.getId(), notFoundUser));
    }

    @Test
    void getUserById_userNotFound_whenThrowException() {
        userDto = userController.createUser(userDto).getBody();
        assertEquals(userDto, userService.getUserById(Objects.requireNonNull(userDto).getId()));

        assertThrows(DataNotFoundException.class, () -> userController.getUserById(99L));
    }

    @Test
    void getAllUsers() {
        assertEquals(userDto.getId(), Objects.requireNonNull(userController.getUserById(Objects.requireNonNull(userDto).getId()).getBody()).getId());
        UserDto userDto1 = UserDto.builder().id(null).name("userName").email("e@mail.com").build();
        userController.createUser(userDto);
        userDto1 = userController.createUser(userDto1).getBody();

        assertTrue(Objects.requireNonNull(userController.getAllUsers().getBody()).contains(userDto));
        assertTrue(Objects.requireNonNull(userController.getAllUsers().getBody()).contains(userDto1));
    }
}
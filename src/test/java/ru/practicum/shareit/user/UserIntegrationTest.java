package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.DataNotFoundException;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    @Order(1)
    void createUserAndGetUserByIdTest() {
        userDto = userController.createUser(userDto).getBody();

        assertEquals(userDto, userService.getUserById(Objects.requireNonNull(userDto).getId()));
    }

    @Test
    @Order(2)
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
    @Order(3)
    void getUserById_userNotFound_whenThrowException() {
        userDto = userController.createUser(userDto).getBody();
        assertEquals(userDto, userService.getUserById(Objects.requireNonNull(userDto).getId()));

        assertThrows(DataNotFoundException.class, () -> userController.getUserById(99L));
    }

    @Test
    @Order(4)
    void deleteUserByIdTest() {
        userDto = userController.createUser(userDto).getBody();
        assertEquals(userDto, userService.getUserById(Objects.requireNonNull(userDto).getId()));

        userController.deleteUserById(userDto.getId());
        assertThrows(DataNotFoundException.class, () -> userService.getUserById(userDto.getId()));
    }

    @Test
    @Order(5)
    void getAllUsers() {
        userDto = userController.createUser(userDto).getBody();
        assertEquals(userDto, userController.getUserById(Objects.requireNonNull(userDto).getId()).getBody());
        UserDto userDto1 = UserDto.builder().id(null).name("userName").email("e@mail.com").build();
        userDto1 = userController.createUser(userDto1).getBody();


        assertEquals(List.of(userDto, Objects.requireNonNull(userDto1)), userController.getAllUsers().getBody());
    }
}
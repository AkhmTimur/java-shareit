package ru.practicum.shareit.user.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;

    @Test
    void createUser_whenInvoked_thenStatusOk() {
        UserDto expectedUser = new UserDto();
        when(userService.createUser(any())).thenReturn(expectedUser);

        ResponseEntity<UserDto> user = userController.createUser(new UserDto());

        assertEquals(HttpStatus.OK, user.getStatusCode());
        assertEquals(expectedUser, user.getBody());
    }

    @Test
    void createUser_whenEmailIsDuplicated_thenStatusConflict() {
        when(userService.createUser(any())).thenThrow(IncorrectDataException.class);

        assertThrows(IncorrectDataException.class, () -> userController.createUser(new UserDto()));
    }

    @Test
    void updateUser_whenInvoked_thenStatusOkAndUserInBody() {
        UserDto expectedUser = new UserDto();
        when(userService.updateUser(any())).thenReturn(expectedUser);

        ResponseEntity<UserDto> user = userController.updateUser(1L, new UserDto());

        assertEquals(HttpStatus.OK, user.getStatusCode());
        assertEquals(expectedUser, user.getBody());
    }

    @Test
    void getUserById_whenInvoked_thenStatusOkAndUserInBody() {
        UserDto expectedUser = new UserDto();
        when(userService.getUserById(any())).thenReturn(expectedUser);

        ResponseEntity<UserDto> user = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, user.getStatusCode());
        assertEquals(expectedUser, user.getBody());
    }

    @Test
    void getAllUsers_whenInvoked_thenStatusOkAndUsersCollectionInBody() {
        List<UserDto> expectedUsers = List.of(new UserDto());
        when(userService.getAllUsers()).thenReturn(expectedUsers);

        ResponseEntity<List<UserDto>> users = userController.getAllUsers();

        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(expectedUsers, users.getBody());
    }
}

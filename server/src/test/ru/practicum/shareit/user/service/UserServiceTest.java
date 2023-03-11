package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.user.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDtoMapper userDtoMapper;
    @InjectMocks
    private UserService userService;
    User userToSave = new User(1L, "email@mail.com", "name");
    UserDto userDto = new UserDto(1L, "email@mail.com", "name");

    @BeforeEach
    void setup() {
        lenient().when(userDtoMapper.userToDto(userToSave))
                .thenReturn(new UserDto(userToSave.getId(), userToSave.getEmail(), userToSave.getName()));
        lenient().when(userDtoMapper.dtoToUser(userDto))
                .thenReturn(new User(userDto.getId(), userDto.getEmail(), userDto.getName()));
    }

    @Test
    void createUser_whenUserValid_thenSaveUser() {
        when(userRepository.save(any(User.class))).thenReturn(userToSave);

        UserDto actualUser = userService.createUser(userDto);

        assertEquals(userToSave.getEmail(), actualUser.getEmail());
        assertEquals(userToSave.getName(), actualUser.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_whenUserNameInvalid_thenIncorrectDataExceptionThrown() {
        userDto.setName(null);
        when(userRepository.save(userDtoMapper.dtoToUser(userDto))).thenThrow(new IncorrectDataException(""));

        assertThrows(IncorrectDataException.class, () -> userService.createUser(userDto));
    }

    @Test
    void updateUser_WhenUserExist_thenUpdateUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userToSave));
        when(userRepository.save(any(User.class))).thenReturn(userToSave);

        UserDto actualUser = userService.updateUser(userDto);

        assertEquals("name", actualUser.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_whenUserNotFound_thenDataNotFoundExceptionThrown() {
        when(userRepository.findById(any())).thenThrow(new DataNotFoundException("Пользователь не найден"));

        assertThrows(DataNotFoundException.class, () -> userService.updateUser(userDto));
        verify(userRepository, never()).save(userToSave);
    }

    @Test
    void getUserById_whenUserFound_thenReturnedUser() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToSave));

        User user = userDtoMapper.dtoToUser(userService.getUserById(userId));

        assertEquals(userToSave, user);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_whenUserNotFound_thenDataNotFoundExceptionThrown() {
        long userId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void deleteUserTest() {
        long userId = 0L;
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUserById(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void getAllUsers_whenUsersEmpty_thenReturnEmptyList() {
        doReturn(Collections.emptyList()).when(userRepository).findAll();

        List<UserDto> userDtos = userService.getAllUsers();

        assertEquals(userDtos, Collections.emptyList());
    }

    @Test
    void getAllUsers_whenUsersNotEmpty_thenReturnListOfUsers() {
        when(userRepository.save(userToSave)).thenReturn(userToSave);
        userService.createUser(userDto);
        when(userRepository.findAll()).thenReturn(List.of(userToSave));

        List<UserDto> actualUsers = userService.getAllUsers();

        assertEquals(List.of(userDto), actualUsers);
    }
}

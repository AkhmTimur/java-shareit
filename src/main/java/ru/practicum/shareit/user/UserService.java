package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final UserDtoMapper userDtoMapper;

    public UserService(@Qualifier("InMemoryUserStorage") UserStorage userStorage, UserDtoMapper userDtoMapper) {
        this.userStorage = userStorage;
        this.userDtoMapper = userDtoMapper;
    }

    public UserDto createUser(UserDto userDto) {
        return userDtoMapper.userToDto(userStorage.addToUsers(userDtoMapper.dtoToUser(userDto)));
    }

    public UserDto updateUser(UserDto userDto) {
        return userDtoMapper.userToDto(userStorage.updateUser(userDtoMapper.dtoToUser(userDto)));
    }

    public UserDto getUserById(Integer userId) {
        return userDtoMapper.userToDto(userStorage.getUserById(userId));
    }

    public void deleteUserById(Integer userId) {
        userStorage.deleteUserById(userId);
    }

    public List<UserDto> getAllUsers() {
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : userStorage.getAllUsers()) {
            userDtos.add(userDtoMapper.userToDto(user));
        }
        return userDtos;
    }
}

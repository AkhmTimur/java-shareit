package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {
    public User dtoToUser(UserDto userDto) {
        return new User(userDto.getId(), userDto.getEmail(), userDto.getName());
    }

    public UserDto userToDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }
}

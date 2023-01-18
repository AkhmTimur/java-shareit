package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.DataConflictException;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;

    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new IncorrectDataException("Некорректная данные");
        }
        return userDtoMapper.userToDto(userRepository.save(userDtoMapper.dtoToUser(userDto)));
    }

    public UserDto updateUser(UserDto userDto) {
        if (userDto.getName() == null) {
            userRepository.findById(userDto.getId()).ifPresent(user -> userDto.setName(user.getName()));
        } else if (userDto.getEmail() == null) {
            userRepository.findById(userDto.getId()).ifPresent(user -> userDto.setEmail(user.getEmail()));
        }
        return userDtoMapper.userToDto(
                userRepository.save(
                        userDtoMapper.dtoToUser(userDto)
                )
        );
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            return userDtoMapper.userToDto(user);
        } else {
            throw new DataNotFoundException("Пользователь не найден");
        }
    }

    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<UserDto> getAllUsers() {
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            userDtos.add(userDtoMapper.userToDto(user));
        }
        return userDtos;
    }
}

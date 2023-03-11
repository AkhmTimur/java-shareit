package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.DataNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;

    @Transactional
    public UserDto createUser(UserDto userDto) {
        return userDtoMapper.userToDto(userRepository.save(userDtoMapper.dtoToUser(userDto)));
    }

    @Transactional
    public UserDto updateUser(UserDto userDto) {
        User user = userRepository.findById(userDto.getId()).orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        if (userDto.getName() == null) {
            userDto.setName(user.getName());
        } else if (userDto.getEmail() == null) {
            userDto.setEmail(user.getEmail());
        }
        User fromDto = userDtoMapper.dtoToUser(userDto);
        User saved = userRepository.save(fromDto);
        return userDtoMapper.userToDto(saved);
    }

    public UserDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(userDtoMapper::userToDto)
                .orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
    }

    @Transactional
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

package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.exceptions.DataConflictException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("InMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private Integer nextId = 0;

    @Override
    public User addToUsers(User user) {
        if (!duplicateCheck(user)) {
            user.setId(genId());
            users.put(user.getId(), user);
            log.trace("Пользователь {} создан", user);
            return user;
        } else {
            log.trace("Пользователь {} уже создан", user);
            throw new DataConflictException("This user " + user + " already was created");
        }
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            User oldUser = users.get(user.getId());
            if (user.getName() != null) {
                oldUser.setName(user.getName());
            }
            if (user.getEmail() != null) {
                if (duplicateCheck(user)) {
                    throw new DataConflictException("Email данного пользователя " + user.getId() +
                            " уже равен " + user.getEmail());
                }
                oldUser.setEmail(user.getEmail());
            }
            users.put(user.getId(), oldUser);
            log.trace("Пользователь {} обновлен", user);
            return users.get(user.getId());
        } else {
            log.trace("Не найден пользователь {}", user);
            throw new DataNotFoundException("Данного пользователя не существует " + user);
        }
    }

    @Override
    public User getUserById(Integer userId) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        } else {
            log.trace("Не найден пользователь c id {}", userId);
            throw new DataNotFoundException("Данного пользователя не существует " + userId);
        }
    }

    @Override
    public void deleteUserById(Integer userId) {
        if (users.containsKey(userId)) {
            users.remove(userId);
        } else {
            log.trace("Не найден пользователь c id {}", userId);
            throw new DataNotFoundException("Данного пользователя не существует " + userId);
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>(users.values());
        result.sort((u1, u2) -> u1.getId() - u2.getId());
        return result;
    }

    private Integer genId() {
        nextId++;
        return nextId;
    }

    private boolean duplicateCheck(User user) {
        for (User userFromList : users.values()) {
            if (userFromList.getEmail() != null && userFromList.getEmail().equals(user.getEmail())) {
                return true;
            }
        }
        return false;
    }
}

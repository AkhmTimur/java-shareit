package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

@DataJpaTest
public class UserRepositoryIT {
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    private void addUsers() {
        userRepository.save(User.builder()
                .email("e@mail.ru")
                .name("name")
                .build());
    }

    @AfterEach
    private void deleteUsers() {
        userRepository.deleteAll();
    }

}

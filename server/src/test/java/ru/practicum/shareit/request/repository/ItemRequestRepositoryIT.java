package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ItemRequestRepositoryIT {
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;
    ItemRequest itemRequest = new ItemRequest();
    User user = new User();

    @BeforeEach
    public void addItemRequest() {
        user = new User(null, "e@mail.ru", "name");
        user = userRepository.save(user);
        itemRequest = new ItemRequest("description", user);

        itemRequestRepository.save(itemRequest);
    }

    @Test
    void findByRequesterId() {
        Long userId = 0L;
        List<ItemRequest> users = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId);

        assertNotNull(users);
    }

    @Test
    void findAllByRequesterIdIsNot() {
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(user.getId());

        assertEquals(List.of(itemRequest), itemRequests);
    }

    @AfterEach
    public void deleteAllItemRequests() {
        itemRequestRepository.deleteAll();
    }
}

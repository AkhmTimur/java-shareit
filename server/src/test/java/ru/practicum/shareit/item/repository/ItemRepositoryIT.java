package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ItemRepositoryIT {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    private User user = new User(null, "e@mail.ru", "name");
    private ItemRequest itemRequest = new ItemRequest("description", user);
    private Item item = new Item(null, "name", "description", true, user, itemRequest);

    @BeforeEach
    void setup() {
        user = userRepository.save(user);
        itemRequest = itemRequestRepository.save(itemRequest);
        item = itemRepository.save(item);
    }

    @Test
    void findByOwnerIdOrderById() {
        Long userId = user.getId();
        List<Item> itemList = itemRepository.findByOwnerIdOrderById(userId);

        assertEquals(itemList, List.of(item));
    }

    @Test
    void searchItems() {
        List<Item> itemList = itemRepository.searchItems("descr", PageRequest.of(0, 10));

        assertEquals(itemList, List.of(item));
    }

    @Test
    void findByRequestIdIn() {
        Long itemRequestId = itemRequest.getId();

        List<Item> itemList = itemRepository.findByRequestIdIn(List.of(itemRequestId));

        assertEquals(itemList, List.of(item));
    }
}

package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.InMemoryUserStorage;

import java.util.*;

@Component("InMemoryItemStorage")
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Integer, Item> items = new HashMap<>();
    private final Map<Integer, List<Integer>> userItems = new HashMap<>();
    private Integer nextId = 0;
    private final InMemoryUserStorage inMemoryUserStorage;

    public InMemoryItemStorage(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    @Override
    public Item createItem(Item item) {
        inMemoryUserStorage.getUserById(item.getOwnerId());
        item.setId(genId());
        items.put(item.getId(), item);
        userItems.put(item.getOwnerId(), List.of(item.getId()));
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        if (Objects.equals(items.get(item.getId()).getOwnerId(), item.getOwnerId())) {
            Item oldItem = items.get(item.getId());
            if (item.getName() != null) {
                oldItem.setName(item.getName());
            }
            if (item.getDescription() != null) {
                oldItem.setDescription(item.getDescription());
            }
            if (item.getAvailable() != null) {
                oldItem.setAvailable(item.getAvailable());
            }
            items.put(item.getId(), oldItem);
            return oldItem;
        } else {
            throw new DataNotFoundException("Предмет " + item + " не зарезервирован пользователем " + item.getOwnerId());
        }
    }

    @Override
    public Item getItemById(Integer itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getAllItemsOfUser(Integer userId) {
        List<Item> result = new ArrayList<>();
        for (Integer itemId : userItems.get(userId)) {
            result.add(items.get(itemId));
        }
        result.sort((u1, u2) -> u1.getId() - u2.getId());
        return result;
    }

    @Override
    public List<Item> searchForItem(String searchCriteria) {
        List<Item> result = new ArrayList<>();
        if (!searchCriteria.isBlank()) {
            for (Item item : items.values()) {
                if (item.getAvailable() &&
                        (item.getDescription().toLowerCase().contains(searchCriteria.toLowerCase()) ||
                                item.getName().toLowerCase().contains(searchCriteria.toLowerCase()))
                ) {
                    result.add(item);
                }
            }
            return result;
        } else {
            return Collections.emptyList();
        }

    }

    private Integer genId() {
        nextId++;
        return nextId;
    }
}

package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.InMemoryUserStorage;

import java.util.*;

@Component("InMemoryItemStorage")
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Integer, Item> items = new HashMap<>();
    private final Map<Integer, Integer> itemUser = new HashMap<>();
    private Integer nextId = 0;
    private final InMemoryUserStorage inMemoryUserStorage;

    public InMemoryItemStorage(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    @Override
    public Item createItem(Item item, Integer userId) {
        if (item.getAvailable() != null &&
                item.getName() != null && !item.getName().isBlank() &&
                item.getDescription() != null && !item.getDescription().isBlank()) {
            inMemoryUserStorage.getUserById(userId);
            item.setId(genId());
            items.put(item.getId(), item);
            itemUser.put(item.getId(), userId);
            return item;
        } else {
            throw new IncorrectDataException("Отправленные данные для премета " + item + " некорректны");
        }
    }

    @Override
    public Item updateItem(Item item, Integer userId) {
        if (Objects.equals(itemUser.get(item.getId()), userId)) {
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
            throw new DataNotFoundException("Предмет " + item + " не зарезервирован пользователем " + userId);
        }
    }

    @Override
    public Item getItemById(Integer itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getAllItemsOfUser(Integer userId) {
        List<Item> result = new ArrayList<>();
        for (Integer itemId : itemUser.keySet()) {
            if (Objects.equals(itemUser.get(itemId), userId)) {
                result.add(items.get(itemId));
            }
        }
        result.sort((u1, u2) -> u1.getId() - u2.getId());
        return result;
    }

    @Override
    public List<Item> searchForItem(String searchCriteria) {
        List<Item> result = new ArrayList<>();
        if (!searchCriteria.isBlank()) {
            for (Item item : items.values()) {
                if (item.getAvailable() && item.getDescription().toLowerCase().contains(searchCriteria.toLowerCase())) {
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

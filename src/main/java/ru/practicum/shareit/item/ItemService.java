package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Service
public class ItemService {
    private final ItemStorage itemStorage;

    public ItemService(@Qualifier("InMemoryItemStorage") ItemStorage itemStorage) {
        this.itemStorage = itemStorage;
    }

    public Item createItem(Item item, Integer userId) {
        return itemStorage.createItem(item, userId);
    }

    public Item updateItem(Item item, Integer userId) {
        return itemStorage.updateItem(item, userId);
    }

    public Item getItemById(Integer itemId) {
        return itemStorage.getItemById(itemId);
    }

    public List<Item> getAllItemsOfUser(Integer userId) {
        return itemStorage.getAllItemsOfUser(userId);
    }

    public List<Item> searchForItem(String searchCriteria) {
        return itemStorage.searchForItem(searchCriteria);
    }
}

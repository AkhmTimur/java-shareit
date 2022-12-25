package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item createItem(Item item, Integer userId);

    Item updateItem(Item item, Integer userId);

    Item getItemById(Integer itemId);

    List<Item> getAllItemsOfUser(Integer userId);

    List<Item> searchForItem(String searchCriteria);
}

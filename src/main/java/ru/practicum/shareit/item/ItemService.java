package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {
    private final ItemStorage itemStorage;
    private final ItemDtoMapper itemDtoMapper;

    public ItemService(@Qualifier("InMemoryItemStorage") ItemStorage itemStorage, ItemDtoMapper itemDtoMapper) {
        this.itemStorage = itemStorage;
        this.itemDtoMapper = itemDtoMapper;
    }

    public ItemDto createItem(ItemDto itemDto, Integer ownerId) {
        return itemDtoMapper.itemToDto(itemStorage.createItem(itemDtoMapper.dtoToItem(itemDto, ownerId)));
    }

    public ItemDto updateItem(ItemDto itemDto, Integer ownerId) {
        return itemDtoMapper.itemToDto(itemStorage.updateItem(itemDtoMapper.dtoToItem(itemDto, ownerId)));
    }

    public ItemDto getItemById(Integer itemId) {
        return itemDtoMapper.itemToDto(itemStorage.getItemById(itemId));
    }

    public List<ItemDto> getAllItemsOfUser(Integer userId) {
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : itemStorage.getAllItemsOfUser(userId)) {
            itemDtos.add(itemDtoMapper.itemToDto(item));
        }
        return itemDtos;
    }

    public List<ItemDto> searchForItem(String searchCriteria) {
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : itemStorage.searchForItem(searchCriteria)) {
            itemDtos.add(itemDtoMapper.itemToDto(item));
        }
        return itemDtos;
    }
}

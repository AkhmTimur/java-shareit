package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Integer ownerId, @RequestBody @Valid ItemDto itemDto) {
        return itemService.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Integer ownerId,
                           @PathVariable Integer itemId,
                           @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);
        return itemService.updateItem(itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Integer itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllItemsOfUser(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        return itemService.getAllItemsOfUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchForItem(@RequestParam(name = "text") String searchCriteria) {
        return itemService.searchForItem(searchCriteria);
    }
}

package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.model.Item;

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
    public Item createItem(@RequestHeader("X-Sharer-User-Id") Integer userId, @RequestBody @Valid Item item) {
        return itemService.createItem(item, userId);
    }

    @PatchMapping("/{itemId}")
    public Item updateItem(@RequestHeader("X-Sharer-User-Id") Integer userId,
                           @PathVariable Integer itemId,
                           @RequestBody @Valid Item item) {
        item.setId(itemId);
        return itemService.updateItem(item, userId);
    }

    @GetMapping("/{itemId}")
    public Item getItemById(@PathVariable Integer itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<Item> getAllItemsOfUser(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        return itemService.getAllItemsOfUser(userId);
    }

    @GetMapping("/search")
    public List<Item> searchForItem(@RequestParam(name = "text") String searchCriteria) {
        return itemService.searchForItem(searchCriteria);
    }
}

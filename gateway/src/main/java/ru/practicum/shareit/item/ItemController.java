package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") Long ownerId, @Valid @RequestBody ItemDto itemDto) {
        return itemClient.createItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                             @PathVariable Long itemId,
                                             @RequestBody ItemDto itemDto) {
        return itemClient.updateItem(ownerId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsOfUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                    @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemClient.getAllItemsOfUser(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchForItem(@RequestParam(name = "text") String searchCriteria,
                                                @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemClient.searchForItem(searchCriteria, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createCommentToItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @PathVariable Long itemId,
                                                      @RequestBody CommentDto commentDto) {
        return itemClient.createCommentToItem(userId, itemId, commentDto);
    }
}

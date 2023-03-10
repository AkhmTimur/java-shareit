package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.comments.dto.CommentDtoMapper;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemDtoMapper {
    private final CommentDtoMapper commentDtoMapper;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    public ItemDtoMapper(CommentDtoMapper commentDtoMapper, CommentRepository commentRepository, ItemRequestRepository itemRequestRepository) {
        this.commentDtoMapper = commentDtoMapper;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    public ItemDto itemToDto(Item item) {
        List<CommentDto> commentList = Collections.emptyList();
        List<Comment> commentDtoList = commentRepository.findByItemId(item.getId());
        if (commentDtoList.size() > 0) {
            commentList = commentDtoList.stream().map(commentDtoMapper::commentToDto).collect(Collectors.toList());
        }
        Long requestId = null;
        if (item.getRequest() != null) {
            requestId = item.getRequest().getId();
        }
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner().getId(),
                commentList,
                requestId
        );
    }

    public Item dtoToItem(ItemDto itemDto, User user) {
        ItemRequest itemRequest;
        if (itemDto.getRequestId() != null) {
            itemRequest = itemRequestRepository.findById(itemDto.getRequestId()).orElse(null);
        } else {
            itemRequest = null;
        }
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                user,
                itemRequest
        );
    }
}

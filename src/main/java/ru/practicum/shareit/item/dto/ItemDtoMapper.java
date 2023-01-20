package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.comments.dto.CommentDtoMapper;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemDtoMapper {
    private final CommentDtoMapper commentDtoMapper;
    private final CommentRepository commentRepository;

    public ItemDtoMapper(CommentDtoMapper commentDtoMapper, CommentRepository commentRepository) {
        this.commentDtoMapper = commentDtoMapper;
        this.commentRepository = commentRepository;
    }

    public ItemDto itemToDto(Item item) {
        List<CommentDto> commentList = Collections.emptyList();
        List<Comment> commentDtoList = commentRepository.findByItemId(item.getId());
        if (commentDtoList.size() > 0) {
            commentList = commentDtoList.stream().map(commentDtoMapper::commentToDto).collect(Collectors.toList());
        }
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner().getId(),
                commentList
        );
    }

    public Item dtoToItem(ItemDto itemDto, User user) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                user
        );
    }
}

package ru.practicum.shareit.item.comments.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.comments.model.Comment;

import java.time.LocalDate;

@Component
public class CommentDtoMapper {
    public CommentDto commentToDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                LocalDate.now()
        );
    }
}

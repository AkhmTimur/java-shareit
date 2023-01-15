package ru.practicum.shareit.booking.item.comments;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.item.comments.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemId(Long itemId);
}

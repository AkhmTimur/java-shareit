package ru.practicum.shareit.item.comments.model;

import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.*;

@Entity(name = "comments")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;
    @ManyToOne
    private Item item;
    @OneToOne
    private User author;

    public Comment(String text, Item item, User author) {
        this.text = text;
        this.item = item;
        this.author = author;
    }
}

package ru.practicum.shareit.booking.item.comments.model;

import lombok.*;
import org.hibernate.Hibernate;
import ru.practicum.shareit.booking.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.util.Objects;

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

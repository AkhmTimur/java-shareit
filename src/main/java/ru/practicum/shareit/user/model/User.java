package ru.practicum.shareit.user.model;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class User {
    private Integer id;
    @Email
    private String email;
    private String name;
}

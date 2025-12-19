package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Data
public class User {
    Long id;
    String email;
    String login;
    String name;
    LocalDate birthday;
}

package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        log.debug("Получен запрос на получение всех пользователей");
        return users.values();
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.debug("Получен запрос на добавление нового пользвателя");
        validateUser(user);
        user.setId(getNextId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Новый пользователь с id {} успешно добавлен. Имя пользователя {}", user.getId(), user.getName());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.debug("Получен запрос на обновление информации о пользователе {}", newUser.getId());
        if (newUser.getId() == null) {
            log.error("Попытка обновления информации о пользователе без указания id");
            throw new ValidationException("ID не может быть пустым");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Попытка обновление информации о несуществующем пользователе с ID {}", newUser.getId());
            throw new ValidationException("Пользователь с ID " + newUser.getId() + " не найден");
        }

        validateUser(newUser);
        User oldUser = users.get(newUser.getId());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setBirthday(newUser.getBirthday());

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            oldUser.setName(newUser.getLogin());
        } else {
            oldUser.setName(newUser.getName());
        }
        log.info("Информация о пользователе с ID {} успешно обновлена", newUser.getId());
        return oldUser;
    }

    private void validateUser(User user) {
        log.debug("Началась валидация пользоватлея {}", user);
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.debug("Валидация не прошла: Email пустой или не содержит @");
            throw new ValidationException("Email не должен быть пустым и содержать @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.debug("Валидация не прошла: Логин пустой или содержит пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.debug("Валидация не прошла: Дата рождения указана неверно");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("Валидация прошла успешно");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

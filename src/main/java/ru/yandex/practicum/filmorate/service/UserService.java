package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private User getUserByIdOrFail(Long userId) {
        if (userId == null) {
            throw new ValidationException("ID пользователя не может быть null");
        }
        return userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private void createNameUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void checkNotSameUser(Long firstId, Long secondId) {
        if (firstId.equals(secondId)) {
            throw new ValidationException("Пользователь не может выполнить действие с самим собой");
        }
    }

    public Collection<User> getAllUsers() {
        log.debug("Получение всех пользователей");
        return userStorage.getAll();
    }

    public User createUser(User user) {
        log.debug("Создание нового пользователя");
        validateUser(user);

        createNameUser(user);
        return userStorage.add(user);
    }

    public User updateUser(User user) {
        log.debug("Обновление пользователя с ID {}", user.getId());

        if (user.getId() == null) {
            log.error("Попытка обновления информации о пользователе без указания id");
            throw new ValidationException("ID не может быть пустым");
        }

        getUserByIdOrFail(user.getId());

        validateUser(user);

        createNameUser(user);

        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        log.debug("Добавление друга: пользователь {} добавляет в друзья пользователя {}", userId, friendId);

        getUserByIdOrFail(userId);
        getUserByIdOrFail(friendId);

        checkNotSameUser(userId, friendId);
        if (userStorage.hasFriendship(friendId, userId)) {
            userStorage.confirmFriend(userId, friendId);
        } else {
            userStorage.addFriend(userId, friendId);
        }

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {

        log.debug("Удаление друга {} у пользователя {}", friendId, userId);

        getUserByIdOrFail(userId);
        getUserByIdOrFail(friendId);

        checkNotSameUser(userId, friendId);
        userStorage.removeFriend(userId, friendId);

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {

        log.debug("Друзья у пользователя с ID {} ", userId);
        getUserByIdOrFail(userId);
        return userStorage.getFriends(userId);
    }

    public void confirmFriend(Long userId, Long friendId) {
        log.debug("Подтверждение заявки: {} подтверждает заявку от {}", userId, friendId);
        getUserByIdOrFail(userId);
        getUserByIdOrFail(friendId);
        checkNotSameUser(userId, friendId);
        userStorage.confirmFriend(userId, friendId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        log.debug("Получение общих друзей пользователей {} и {}", userId, otherUserId);

        getUserByIdOrFail(userId);
        getUserByIdOrFail(otherUserId);

        return userStorage.getCommonFriends(userId, otherUserId);
    }

    private void validateUser(User user) {
        log.debug("Началась валидация пользователя {}", user);
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.debug("Валидация не прошла: Email пустой или не содержит @");
            throw new ValidationException("Email не должен быть пустым и содержать @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.debug("Валидация не прошла: Логин пустой или содержит пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() == null) {
            log.debug("Валидация не прошла: Дата рождения null");
            throw new ValidationException("Дата рождения не может быть пустой");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.debug("Валидация не прошла: Дата рождения указана неверно");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("Валидация прошла успешно");
    }
}

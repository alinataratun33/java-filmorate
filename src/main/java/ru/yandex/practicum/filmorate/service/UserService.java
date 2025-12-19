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
    private final Map<Long, Set<Long>> friends = new HashMap<>();

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        log.debug("Получение всех пользователей");
        return userStorage.getAll();
    }

    public User createUser(User user) {
        log.debug("Создание нового пользователя");
        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.add(user);
    }

    public User updateUser(User user) {
        log.debug("Обновление пользователя с ID {}", user.getId());

        if (user.getId() == null) {
            log.error("Попытка обновления информации о пользователе без указания id");
            throw new ValidationException("ID не может быть пустым");
        }

        if (userStorage.getById(user.getId()) == null) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {

        log.debug("Добавление друга {} пользователю {}", friendId, userId);

        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (userStorage.getById(friendId) == null) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }

        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }
        if (!friends.containsKey(userId)) {
            friends.put(userId, new HashSet<>());
        }
        if (!friends.containsKey(friendId)) {
            friends.put(friendId, new HashSet<>());
        }

        friends.get(userId).add(friendId);
        friends.get(friendId).add(userId);

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {

        log.debug("Удаление друга {} у пользователя {}", friendId, userId);

        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (userStorage.getById(friendId) == null) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может удалить себя из друзей");
        }

        if (friends.containsKey(userId)) {
            friends.get(userId).remove(friendId);
        }

        if (friends.containsKey(friendId)) {
            friends.get(friendId).remove(userId);
        }

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {

        log.debug("Друзья у пользователя с ID {} ", userId);

        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Set<Long> friendIds = friends.getOrDefault(userId, new HashSet<>());
        List<User> friendList = new ArrayList<>();

        for (Long friendId : friendIds) {
            User user = userStorage.getById(friendId);
            friendList.add(user);
        }
        return friendList;
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        log.debug("Получение общих друзей пользователей {} и {}", userId, otherUserId);

        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (userStorage.getById(otherUserId) == null) {
            throw new NotFoundException("Пользователь с ID " + otherUserId + " не найден");
        }

        Set<Long> userFriends = friends.getOrDefault(userId, new HashSet<>());
        Set<Long> otherUserFriends = friends.getOrDefault(otherUserId, new HashSet<>());

        Set<Long> commonFriendIds = new HashSet<>(userFriends);
        commonFriendIds.retainAll(otherUserFriends);
        List<User> commonFriends = new ArrayList<>();
        for (Long friendId : commonFriendIds) {
            User user = userStorage.getById(friendId);
            commonFriends.add(user);
        }
        return commonFriends;
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

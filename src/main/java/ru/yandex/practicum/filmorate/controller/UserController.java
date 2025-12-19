package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.debug("Получен запрос на получение всех пользователей");
        return userService.getAllUsers();
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.debug("Получен запрос на добавление нового пользователя");
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.debug("Получен запрос на обновление информации о пользователе {}", newUser.getId());
        return userService.updateUser(newUser);
    }

    @GetMapping("{id}/friends")
    public Collection<User> getAllFriends(@PathVariable Long id) {
        log.debug("Получен запрос на получение всех друзей пользователя {} ", id);
        return userService.getFriends(id);
    }

    @GetMapping("{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.debug("Получен запрос на получение общих друзей пользователя {} с пользователем {} ", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @PutMapping("{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("Получен запрос на добавление друга {} к пользователю {}", friendId, id);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
       log.debug("Получен запрос на удаление друга {} у пользователя {} ", friendId, id);
       userService.deleteFriend(id, friendId);
    }
}

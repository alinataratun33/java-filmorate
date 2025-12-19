package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void testUserEmail() {
        User user = new User();
        user.setName("Name");
        user.setEmail(" ");
        user.setLogin("Login");
        user.setBirthday(LocalDate.of(2001, 6, 8));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.addUser(user);
        });
        assertEquals("Email не должен быть пустым и содержать @", exception.getMessage());
    }

    @Test
    void testUserEmailWithoutChar() {
        User user = new User();
        user.setName("Name");
        user.setEmail("Email");
        user.setLogin("Login");
        user.setBirthday(LocalDate.of(2001, 6, 8));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.addUser(user);
        });
        assertEquals("Email не должен быть пустым и содержать @", exception.getMessage());
    }

    @Test
    void testUserEmptyLogin() {
        User user = new User();
        user.setName("Name");
        user.setEmail("Email@gmail.com");
        user.setLogin(" ");
        user.setBirthday(LocalDate.of(2001, 6, 8));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.addUser(user);
        });
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void testUserLoginWithSpace() {
        User user = new User();
        user.setName("Name");
        user.setEmail("Email@gmail.com");
        user.setLogin("Login login");
        user.setBirthday(LocalDate.of(2001, 6, 8));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.addUser(user);
        });
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void testUserBirthday() {
        User user = new User();
        user.setName("Name");
        user.setEmail("Email@gmail.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2026, 6, 8));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.addUser(user);
        });
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void testUserNameEmpty() {
        User user = new User();
        user.setName(" ");
        user.setEmail("Email@gmail.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2001, 6, 8));

        userController.addUser(user);
        assertEquals(user.getLogin(), user.getName(), "В качестве имени должен использоваться логин");
    }

    @Test
    void testAddUserCorrect() {
        User user = new User();
        user.setName("Name");
        user.setEmail("Email@gmail.com");
        user.setLogin("Login");
        user.setBirthday(LocalDate.of(2001, 6, 8));

        User addedUser = userController.addUser(user);

        assertNotNull(addedUser.getId(), "Пользователь должен получить ID");
        assertEquals("Email@gmail.com", addedUser.getEmail());
        assertEquals("Login", addedUser.getLogin());
        assertEquals("Name", addedUser.getName());
        assertEquals(LocalDate.of(2001, 6, 8), addedUser.getBirthday());

        Collection<User> allUsers = userController.getAllUsers();
        assertEquals(1, allUsers.size());
    }

    @Test
    void testUpdateUserCorrect() {
        User user = new User();
        user.setName("Name");
        user.setEmail("Email@gmail.com");
        user.setLogin("Login");
        user.setBirthday(LocalDate.of(2001, 6, 8));

        User addedUser = userController.addUser(user);
        Long userId = addedUser.getId();

        User updatedData = new User();
        updatedData.setId(userId); // Важно: тот же ID!
        updatedData.setEmail("updated@example.com");
        updatedData.setLogin("updatedlogin");
        updatedData.setName("Updated Name");
        updatedData.setBirthday(LocalDate.of(1991, 1, 1));

        User updatedUser = userController.updateUser(updatedData);

        assertEquals(userId, updatedUser.getId(), "ID должен остаться прежним");
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("updatedlogin", updatedUser.getLogin());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(LocalDate.of(1991, 1, 1), updatedUser.getBirthday());

        assertEquals(1, userController.getAllUsers().size());
    }


}

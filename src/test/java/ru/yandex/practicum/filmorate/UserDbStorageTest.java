package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {

        testUser1 = createTestUser("user1@test.com", "user1", "User 1",
                LocalDate.of(1990, 1, 1));
        testUser1 = userDbStorage.add(testUser1);

        testUser2 = createTestUser("user2@test.com", "user2", "User 2",
                LocalDate.of(1992, 2, 2));
        testUser2 = userDbStorage.add(testUser2);

        testUser3 = createTestUser("user3@test.com", "user3", "User 3",
                LocalDate.of(1994, 3, 3));
        testUser3 = userDbStorage.add(testUser3);
    }

    private User createTestUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    @Test
    void addUserTest() {
        User newUser = createTestUser("newuser@test.com", "newuser", "New User",
                LocalDate.of(1995, 5, 5));

        User addedUser = userDbStorage.add(newUser);

        assertNotNull(addedUser, "Добавленный пользователь не должен быть null");
        assertEquals("newuser@test.com", addedUser.getEmail());
        assertEquals("newuser", addedUser.getLogin());
        assertEquals("New User", addedUser.getName());
        assertEquals(LocalDate.of(1995, 5, 5), addedUser.getBirthday());
    }

    @Test
    void getAllUsersTest() {
        Collection<User> users = userDbStorage.getAll();

        assertNotNull(users, "Список пользователей не должен быть null");
        assertEquals(3, users.size(), "Должно быть 3 пользователя");
    }

    @Test
    void getUserByIdTest() {
        Optional<User> foundUser = userDbStorage.getById(testUser1.getId());

        User user = foundUser.get();
        assertEquals(testUser1.getId(), user.getId());
        assertEquals("user1@test.com", user.getEmail());
        assertEquals("user1", user.getLogin());
        assertEquals("User 1", user.getName());
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthday());
    }

    @Test
    void updateUserTest() {

        testUser1.setEmail("updated@test.com");
        testUser1.setLogin("updatedlogin");
        testUser1.setName("Updated Name");
        testUser1.setBirthday(LocalDate.of(1991, 2, 3));

        User updatedUser = userDbStorage.update(testUser1);
        Optional<User> foundUser = userDbStorage.getById(testUser1.getId());

        assertEquals("updated@test.com", foundUser.get().getEmail());
    }

    @Test
    void addFriendTest() {
        userDbStorage.addFriend(testUser1.getId(), testUser2.getId());

        String sql = "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ? AND status_id = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                testUser1.getId(), testUser2.getId());
        assertEquals(1, count, "Должна быть 1 запись о дружбе со статусом 1");
    }

    @Test
    void hasFriendshipTest() {

        userDbStorage.addFriend(testUser1.getId(), testUser2.getId());

        boolean hasFriendship = userDbStorage.hasFriendship(testUser1.getId(), testUser2.getId());
        boolean hasReverseFriendship = userDbStorage.hasFriendship(testUser2.getId(), testUser1.getId());

        assertTrue(hasFriendship, "Дружба должна существовать");
        assertFalse(hasReverseFriendship, "Обратная дружба не должна существовать");
    }

    @Test
    void confirmFriendTest() {

        userDbStorage.addFriend(testUser1.getId(), testUser2.getId());

        String checkSql = "SELECT status_id FROM friendship WHERE user_id = ? AND friend_id = ?";
        Integer statusBefore = jdbcTemplate.queryForObject(checkSql, Integer.class,
                testUser1.getId(), testUser2.getId());
        assertEquals(1, statusBefore, "Статус должен быть 1 (неподтвержденная)");


        userDbStorage.confirmFriend(testUser2.getId(), testUser1.getId());


        Integer statusAfter = jdbcTemplate.queryForObject(checkSql, Integer.class,
                testUser1.getId(), testUser2.getId());
        assertEquals(2, statusAfter, "Статус исходной заявки должен стать 2");

        String reverseCheckSql = "SELECT status_id FROM friendship WHERE user_id = ? AND friend_id = ?";
        Integer reverseStatus = jdbcTemplate.queryForObject(reverseCheckSql, Integer.class,
                testUser2.getId(), testUser1.getId());
        assertEquals(2, reverseStatus, "Должна создаться обратная заявка со статусом 2");
    }

    @Test
    void getFriendsTest() {

        userDbStorage.addFriend(testUser1.getId(), testUser2.getId());

        List<User> friends = userDbStorage.getFriends(testUser1.getId());

        assertNotNull(friends, "Список друзей не должен быть null");
        assertEquals(1, friends.size(), "У user1 должен быть 1 друг");
    }

    @Test
    void removeFriendTest() {
        userDbStorage.addFriend(testUser1.getId(), testUser2.getId());
        userDbStorage.confirmFriend(testUser2.getId(), testUser1.getId());

        userDbStorage.removeFriend(testUser1.getId(), testUser2.getId());
        Integer afterCount1 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?",
                Integer.class, testUser1.getId(), testUser2.getId());
        assertEquals(0, afterCount1, "Запись user1->user2 должна быть удалена");
    }

    @Test
    void getCommonFriendsTest() {

        userDbStorage.addFriend(testUser1.getId(), testUser3.getId());
        userDbStorage.addFriend(testUser2.getId(), testUser3.getId());

        List<User> commonFriends = userDbStorage.getCommonFriends(testUser1.getId(), testUser2.getId());

        assertNotNull(commonFriends, "Список общих друзей не должен быть null");
        assertEquals(1, commonFriends.size(), "Должен быть 1 общий друг");
        assertEquals(testUser3.getId(), commonFriends.get(0).getId(), "Общим другом должен быть user3");
    }
}

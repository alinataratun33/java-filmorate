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
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;

    private User testUser1;
    private User testUser2;
    private Film testFilm1;
    private Film testFilm2;

    private Film createTestFilm(String name, String description,
                                LocalDate releaseDate, int duration, long mpaId) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);

        Mpa mpa = new Mpa();
        mpa.setId(mpaId);
        film.setMpa(mpa);

        return film;
    }

    private User createTestUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    @BeforeEach
    void setUp() {
        User user1 = createTestUser("user1@test.com", "user1", "User 1",
                LocalDate.of(1990, 1, 1));
        testUser1 = userDbStorage.add(user1);

        User user2 = createTestUser("user2@test.com", "user2", "User 2",
                LocalDate.of(1992, 2, 2));
        testUser2 = userDbStorage.add(user2);

        testFilm1 = createTestFilm("Film 1", "Description 1",
                LocalDate.of(2000, 1, 1), 120, 1);
        testFilm1 = filmDbStorage.add(testFilm1);

        testFilm2 = createTestFilm("Film 2", "Description 2",
                LocalDate.of(2001, 1, 1), 130, 2);
        testFilm2 = filmDbStorage.add(testFilm2);
    }

    @Test
    void addFilmTest() {
        Film film = createTestFilm("Test Film", "Test Description",
                LocalDate.of(2000, 1, 1), 120, 1);

        Film addedFilm = filmDbStorage.add(film);

        assertNotNull(addedFilm, "Добавленный фильм не должен быть null");

        assertEquals("Test Film", addedFilm.getName());
        assertEquals("Test Description", addedFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), addedFilm.getReleaseDate());
        assertEquals(120, addedFilm.getDuration());
    }

    @Test
    void testGetById() {
        Optional<Film> foundFilm = filmDbStorage.getById(testFilm1.getId());

        Film retrievedFilm = foundFilm.get();
        assertNotNull(retrievedFilm, "Найденный фильм не должен быть null");
        assertEquals(testFilm1.getId(), retrievedFilm.getId());
        assertEquals("Film 1", retrievedFilm.getName());

    }

    @Test
    void updateFilmTest() {
        testFilm1.setName("Updated Film");
        testFilm1.setDescription("Updated Description");
        testFilm1.setReleaseDate(LocalDate.of(2005, 5, 5));
        testFilm1.setDuration(150);

        Mpa newMpa = new Mpa();
        newMpa.setId(3L);
        testFilm1.setMpa(newMpa);

        Film updatedFilm = filmDbStorage.update(testFilm1);

        Optional<Film> foundFilm = filmDbStorage.getById(testFilm1.getId());

        assertEquals("Updated Film", foundFilm.get().getName());
    }

    @Test
    void addLikeTest() {

        filmDbStorage.addLike(testFilm1.getId(), testUser1.getId());

        String sql = "SELECT COUNT(*) FROM film_like WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                testFilm1.getId(), testUser1.getId());
        assertEquals(1, count, "В таблице film_like должна быть 1 запись");
    }

    @Test
    void removeLikeTest() {

        filmDbStorage.addLike(testFilm1.getId(), testUser1.getId());


        String checkSql = "SELECT COUNT(*) FROM film_like WHERE film_id = ? AND user_id = ?";

        filmDbStorage.removeLike(testFilm1.getId(), testUser1.getId());

        Integer countAfterRemove = jdbcTemplate.queryForObject(
                checkSql,
                Integer.class,
                testFilm1.getId(),
                testUser1.getId()
        );
        assertEquals(0, countAfterRemove, "После удаления не должно быть записей");
    }

    @Test
    void popularFilmsTest() {

        filmDbStorage.addLike(testFilm1.getId(), testUser1.getId());
        filmDbStorage.addLike(testFilm1.getId(), testUser2.getId());

        filmDbStorage.addLike(testFilm2.getId(), testUser1.getId());

        List<Film> popularFilms = filmDbStorage.getPopularFilms(10);

        assertNotNull(popularFilms);
        assertEquals(2, popularFilms.size(), "Должно быть 2 фильма");
        assertEquals(testFilm1.getId(), popularFilms.get(0).getId(),
                "Film 1 должен быть первым (2 лайка)");
        assertEquals(testFilm2.getId(), popularFilms.get(1).getId(),
                "Film 2 должен быть вторым (1 лайк)");
    }

    @Test
    void getAllFilmsTest() {

        Collection<Film> allFilms = filmDbStorage.getAll();

        assertNotNull(allFilms);
        assertEquals(2, allFilms.size(), "Должно быть 2 фильма");
    }
}
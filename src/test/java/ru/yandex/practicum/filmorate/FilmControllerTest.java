package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmController filmController;
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void setUp() {

        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();

        userService = new UserService(userStorage);

        filmService = new FilmService(filmStorage, userStorage);

        filmController = new FilmController(filmService);
    }


    @Test
    void testFilmWithEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void testFilmDescription() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Описание не может быть больше 200 символов", exception.getMessage());
    }

    @Test
    void testFilmRealiseDate() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1822, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Дата релиза не может быть раньше 1895-12-28", exception.getMessage());
    }

    @Test
    void testFilmDuration() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Продолжительность фильма не может быть отрицательным числом или 0", exception.getMessage());
    }

    @Test
    void testAddCorrectFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film addedFilm = filmController.addFilm(film);
        assertNotNull(addedFilm.getId(), "Фильм должен получить ID");
        assertEquals("Film", addedFilm.getName());
        assertEquals(120, addedFilm.getDuration());
        assertEquals(LocalDate.of(2000, 1, 1), addedFilm.getReleaseDate());

        Collection<Film> allFilms = filmController.getAllFilms();
        assertEquals(1, allFilms.size());
    }

    @Test
    void testUpdateCorrectFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film addedFilm = filmController.addFilm(film);
        Long filmId = addedFilm.getId();

        Film updatedData = new Film();
        updatedData.setId(filmId);
        updatedData.setName("Updated Name");
        updatedData.setDescription("Updated Description");
        updatedData.setReleaseDate(LocalDate.of(2001, 1, 1));
        updatedData.setDuration(150);

        Film updatedFilm = filmController.updateFilm(updatedData);

        assertEquals(filmId, updatedFilm.getId(), "ID должен остаться прежним");
        assertEquals("Updated Name", updatedFilm.getName());
        assertEquals("Updated Description", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2001, 1, 1), updatedFilm.getReleaseDate());
        assertEquals(150, updatedFilm.getDuration());

        assertEquals(1, filmController.getAllFilms().size());
    }
}

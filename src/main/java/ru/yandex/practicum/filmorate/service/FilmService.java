package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, GenreStorage genreStorage, MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    private Film getFilmByIdOrFail(Long filmId) {
        if (filmId == null) {
            throw new ValidationException("ID фильма не может быть null");
        }
        return filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
    }

    private User getUserByIdOrFail(Long userId) {
        if (userId == null) {
            throw new ValidationException("ID пользователя не может быть null");
        }
        return userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    public Collection<Film> getAllFilms() {
        log.debug("Получение всех фильмов");
        return filmStorage.getAll();
    }

    public Film getFilmById(Long id) {
        log.debug("Получение фильма с ID: {}", id);

        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public Film addFilm(Film film) {
        log.debug("Добавление фильма");
        validateFilm(film);

        mpaStorage.getById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("MPA рейтинг с id " + film.getMpa().getId() + " не найден"));

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (genre.getId() != null) {
                    genreStorage.getById(genre.getId())
                            .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + " не найден"));
                }
            }
        }
        return filmStorage.add(film);
    }

    public Film updateFilm(Film newFilm) {
        log.debug("Обновление информации о фильме с ID" + newFilm.getId());
        if (newFilm.getId() == null) {
            log.error("Попытка обновления фильма без указания ID");
            throw new ValidationException("ID не может быть пустым");
        }
        getFilmByIdOrFail(newFilm.getId());
        validateFilm(newFilm);

        return filmStorage.update(newFilm);
    }

    public void addLike(Long filmId, Long userId) {
        getFilmByIdOrFail(filmId);
        getUserByIdOrFail(userId);

        filmStorage.addLike(filmId, userId);

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        getFilmByIdOrFail(filmId);
        getUserByIdOrFail(userId);

        filmStorage.removeLike(filmId, userId);

        log.info("Пользователь {} удалил лайк фильма {}", userId, filmId);
    }

    public Collection<Film> getPopularFilms(Integer count) {
        log.debug("Получение {} популярных фильмов", count);
        if (count == null || count <= 0) {
            count = 10;
        }

        return filmStorage.getPopularFilms(count);
    }

    private void validateFilm(Film film) {
        log.debug("Начата валидация фильма {}", film);
        if (film.getName() == null || film.getName().isBlank()) {
            log.debug("Валидация не прошла: название фильма пустое");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() == null) {
            log.debug("Валидация не прошла: описание фильма null");
            throw new ValidationException("Описание не может быть пустым");
        }
        if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.debug("Валидация не прошла: лимит символов превышен");
            throw new ValidationException("Описание не может быть больше " + MAX_DESCRIPTION_LENGTH + " символов");
        }
        if (film.getReleaseDate() == null) {
            log.debug("Валидация не прошла: дата релиза null");
            throw new ValidationException("Дата релиза не может быть пустой");
        }
        if (film.getReleaseDate().isBefore((MIN_RELEASE_DATE))) {
            log.debug("Валидация не прошла: дата релиза указана неверно");
            throw new ValidationException("Дата релиза не может быть раньше " + MIN_RELEASE_DATE);
        }
        if (film.getDuration() == null) {
            log.debug("Валидация не прошла: продолжительность фильма null");
            throw new ValidationException("Продолжительность фильма не может быть пустой");
        }
        if (film.getDuration() <= 0) {
            log.debug("Валидация не прошла: продолжительность фильма отрицательная или равна 0");
            throw new ValidationException("Продолжительность фильма не может быть отрицательным числом или 0");
        }
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            log.debug("Валидация не прошла: рейтинг MPA должен быть указан");
            throw new ValidationException("MPA рейтинг должен быть указан");
        }
        log.debug("Валидация фильма прошла успешно");
    }
}

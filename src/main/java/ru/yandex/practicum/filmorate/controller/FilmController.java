package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.debug("Получен запрос на получение всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.debug("Получен запрос на добавление фильма");
        validateFilm(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм с ID {} успешно добавлен. Название: '{}'", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.debug("Получен запрос на обновление информации о фильме с ID" + newFilm.getId());
        if (newFilm.getId() == null) {
            log.error("Попытка обновления фильма без указания ID");
            throw new ValidationException("ID не может быть пустым");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.warn("Попытка обновления несуществующего фильма с ID: {}", newFilm.getId());
            throw new ValidationException("Фильм с ID " + newFilm.getId() + " не найден");
        }
        validateFilm(newFilm);

        Film oldFilm = films.get(newFilm.getId());
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setDuration(newFilm.getDuration());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        log.info("Фильм с ID {} успешно обновлен", newFilm.getId());
        return oldFilm;
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
        log.debug("Валидация фильма прошла успешно");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

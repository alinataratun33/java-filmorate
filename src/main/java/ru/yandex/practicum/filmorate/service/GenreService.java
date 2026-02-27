package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Service
@Slf4j
public class GenreService {
    private final GenreStorage genreStorage;


    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<Genre> getAllGenres() {
        log.debug("Получение всех жанров");
        return genreStorage.getAll();
    }

    public Genre getGenreById(Long id) {
        log.debug("Запрос на получение жанра с ID={}", id);

        if (id == null) {
            throw new ValidationException("ID жанра не может быть null");
        }

        if (id <= 0) {
            throw new ValidationException("ID жанра должен быть положительным числом");
        }

        return genreStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
    }
}

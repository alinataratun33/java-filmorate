package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GenreStorage {
    Collection<Genre> getAll();

    Optional<Genre> getById(Long id);

    void saveGenresForFilm(Long filmId, List<Genre> genres);

    void updateGenresForFilm(Long filmId, List<Genre> genres);

    Map<Long, List<Genre>> getGenresForAllFilms();
}

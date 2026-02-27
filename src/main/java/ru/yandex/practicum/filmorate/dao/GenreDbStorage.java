package ru.yandex.practicum.filmorate.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.*;

@Repository
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public Collection<Genre> getAll() {
        String sql = "SELECT * FROM genre ORDER BY genre_id";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    @Override
    public Optional<Genre> getById(Long id) {
        String sql = "SELECT * FROM genre WHERE genre_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper, id);

        if (genres.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(genres.getFirst());
    }

    @Override
    public Map<Long, List<Genre>> getGenresForAllFilms() {
        String sql = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genre fg JOIN genre g ON fg.genre_id = g.genre_id";

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, List<Genre>> filmGenresMap = new HashMap<>();

            while (rs.next()) {
                Long filmId = rs.getLong("film_id");

                Genre genre = new Genre();
                genre.setId(rs.getLong("genre_id"));
                genre.setName(rs.getString("name"));

                if (!filmGenresMap.containsKey(filmId)) {
                    filmGenresMap.put(filmId, new ArrayList<>());
                }
                filmGenresMap.get(filmId).add(genre);
            }

            return filmGenresMap;
        });
    }

    @Override
    public void saveGenresForFilm(Long filmId, List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        Set<Long> uniqueGenreIds = new HashSet<>();
        for (Genre genre : genres) {
            if (genre != null && genre.getId() != null) {
                uniqueGenreIds.add(genre.getId());
            }
        }

        for (Long genreId : uniqueGenreIds) {
            jdbcTemplate.update(sql, filmId, genreId);
        }
    }

    @Override
    public void updateGenresForFilm(Long filmId, List<Genre> genres) {
        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);

        if (genres != null && !genres.isEmpty()) {
            saveGenresForFilm(filmId, genres);
        }
    }
}

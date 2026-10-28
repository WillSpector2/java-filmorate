package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository("genreDbStorage")
public class GenreDbStorage implements GenreStorage {

    private static final String FIND_ALL_GENRES_QUERY =
            "SELECT id, name FROM genres ORDER BY id";

    private static final String FIND_GENRE_BY_ID_QUERY =
            "SELECT id, name FROM genres WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getAll() {
        return jdbcTemplate.query(
                FIND_ALL_GENRES_QUERY,
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                }
        );
    }

    @Override
    public Genre getById(int id) {
        try {
            return jdbcTemplate.queryForObject(
                    FIND_GENRE_BY_ID_QUERY,
                    (rs, rowNum) -> {
                        Genre genre = new Genre();
                        genre.setId(rs.getInt("id"));
                        genre.setName(rs.getString("name"));
                        return genre;
                    },
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(
                    "Жанр с id=" + id + " не найден"
            );
        }
    }
}
package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private static final String INSERT_FILM_QUERY = """
            INSERT INTO films
                (name, description, release_date, duration, mpa_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_FILM_QUERY = """
            UPDATE films
            SET name = ?,
                description = ?,
                release_date = ?,
                duration = ?,
                mpa_id = ?
            WHERE id = ?
            """;

    private static final String FIND_FILM_BY_ID_QUERY = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   f.mpa_id,
                   m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            WHERE f.id = ?
            """;

    private static final String FIND_ALL_FILM_IDS_QUERY =
            "SELECT id FROM films ORDER BY id";

    private static final String DELETE_FILM_QUERY =
            "DELETE FROM films WHERE id = ?";

    private static final String FIND_GENRES_BY_FILM_ID_QUERY = """
            SELECT g.id, g.name
            FROM genres g
            JOIN film_genres fg ON fg.genre_id = g.id
            WHERE fg.film_id = ?
            ORDER BY g.id
            """;

    private static final String FIND_LIKES_BY_FILM_ID_QUERY = """
            SELECT user_id
            FROM film_likes
            WHERE film_id = ?
            ORDER BY user_id
            """;

    private static final String DELETE_FILM_GENRES_QUERY =
            "DELETE FROM film_genres WHERE film_id = ?";

    private static final String INSERT_FILM_GENRE_QUERY = """
            INSERT INTO film_genres (film_id, genre_id)
            VALUES (?, ?)
            """;

    private static final String DELETE_FILM_LIKES_QUERY =
            "DELETE FROM film_likes WHERE film_id = ?";

    private static final String INSERT_FILM_LIKE_QUERY = """
            INSERT INTO film_likes (film_id, user_id)
            VALUES (?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    public FilmDbStorage(
            JdbcTemplate jdbcTemplate,
            FilmRowMapper filmRowMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
    }

    @Override
    @Transactional
    public Film add(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    INSERT_FILM_QUERY,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(
                    3,
                    film.getReleaseDate() == null
                            ? null
                            : Date.valueOf(film.getReleaseDate())
            );
            statement.setInt(4, film.getDuration());

            if (film.getMpa() == null) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setInt(5, film.getMpa().getId());
            }

            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException(
                    "Не удалось получить id созданного фильма"
            );
        }

        film.setId(generatedId.intValue());

        saveGenres(film);
        saveLikes(film);

        return getById(film.getId());
    }

    @Override
    @Transactional
    public Film update(Film film) {
        Film existingFilm = getById(film.getId());

        int updatedRows = jdbcTemplate.update(
                UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() == null
                        ? null
                        : Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId(),
                film.getId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException(
                    "Film with id=" + film.getId() + " not found"
            );
        }

        if (film.getGenres() == null) {
            film.setGenres(existingFilm.getGenres());
        }

        if (film.getLikes() == null) {
            film.setLikes(existingFilm.getLikes());
        }

        saveGenres(film);
        saveLikes(film);

        return getById(film.getId());
    }

    @Override
    public Film getById(int id) {
        Film film;

        try {
            film = jdbcTemplate.queryForObject(
                    FIND_FILM_BY_ID_QUERY,
                    filmRowMapper,
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(
                    "Film with id=" + id + " not found"
            );
        }

        film.setGenres(getGenres(id));
        film.setLikes(getLikes(id));

        return film;
    }

    @Override
    public List<Film> getAll() {
        List<Integer> filmIds = jdbcTemplate.query(
                FIND_ALL_FILM_IDS_QUERY,
                (resultSet, rowNum) -> resultSet.getInt("id")
        );

        List<Film> films = new ArrayList<>();

        for (Integer filmId : filmIds) {
            films.add(getById(filmId));
        }

        return films;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        return jdbcTemplate.update(DELETE_FILM_QUERY, id) > 0;
    }

    private List<Genre> getGenres(int filmId) {
        return jdbcTemplate.query(
                FIND_GENRES_BY_FILM_ID_QUERY,
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                },
                filmId
        );
    }

    private Set<Integer> getLikes(int filmId) {
        List<Integer> userIds = jdbcTemplate.query(
                FIND_LIKES_BY_FILM_ID_QUERY,
                (rs, rowNum) -> rs.getInt("user_id"),
                filmId
        );

        return new HashSet<>(userIds);
    }

    private void saveGenres(Film film) {
        jdbcTemplate.update(DELETE_FILM_GENRES_QUERY, film.getId());

        if (film.getGenres() == null) {
            return;
        }

        Set<Integer> genreIds = new HashSet<>();

        for (Genre genre : film.getGenres()) {
            if (genre != null && genreIds.add(genre.getId())) {
                jdbcTemplate.update(
                        INSERT_FILM_GENRE_QUERY,
                        film.getId(),
                        genre.getId()
                );
            }
        }
    }

    private void saveLikes(Film film) {
        jdbcTemplate.update(DELETE_FILM_LIKES_QUERY, film.getId());

        if (film.getLikes() == null) {
            return;
        }

        Set<Integer> uniqueLikes = new HashSet<>(film.getLikes());

        for (Integer userId : uniqueLikes) {
            jdbcTemplate.update(
                    INSERT_FILM_LIKE_QUERY,
                    film.getId(),
                    userId
            );
        }
    }
}
package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Film> filmRowMapper = this::mapFilm;

    @Override
    @Transactional
    public Film add(Film film) {
        String sql = """
                INSERT INTO films
                    (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS
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
                statement.setNull(5, java.sql.Types.INTEGER);
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
        // Сначала проверяем существование фильма и сохраняем его связи.
        Film existingFilm = getById(film.getId());

        String sql = """
                UPDATE films
                SET name = ?,
                    description = ?,
                    release_date = ?,
                    duration = ?,
                    mpa_id = ?
                WHERE id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
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

        // Если связи не переданы, сохраняем существующие.
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
        List<Film> films = jdbcTemplate.query(
                """
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
                """,
                filmRowMapper,
                id
        );

        if (films.isEmpty()) {
            throw new NotFoundException(
                    "Film with id=" + id + " not found"
            );
        }

        Film film = films.get(0);
        film.setGenres(getGenres(id));
        film.setLikes(getLikes(id));

        return film;
    }

    @Override
    public List<Film> getAll() {
        List<Integer> filmIds = jdbcTemplate.query(
                "SELECT id FROM films ORDER BY id",
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
        return jdbcTemplate.update(
                "DELETE FROM films WHERE id = ?",
                id
        ) > 0;
    }

    private Film mapFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        Date releaseDate = rs.getDate("release_date");
        film.setReleaseDate(
                releaseDate == null ? null : releaseDate.toLocalDate()
        );

        film.setDuration(rs.getInt("duration"));

        Integer mpaId = rs.getObject("mpa_id", Integer.class);

        if (mpaId != null) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }

        return film;
    }

    private List<Genre> getGenres(int filmId) {
        return jdbcTemplate.query(
                """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """,
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
                """
                SELECT user_id
                FROM film_likes
                WHERE film_id = ?
                ORDER BY user_id
                """,
                (rs, rowNum) -> rs.getInt("user_id"),
                filmId
        );

        return new HashSet<>(userIds);
    }

    private void saveGenres(Film film) {
        jdbcTemplate.update(
                "DELETE FROM film_genres WHERE film_id = ?",
                film.getId()
        );

        if (film.getGenres() == null) {
            return;
        }

        Set<Integer> genreIds = new HashSet<>();

        for (Genre genre : film.getGenres()) {
            if (genre != null && genreIds.add(genre.getId())) {
                jdbcTemplate.update(
                        """
                        INSERT INTO film_genres (film_id, genre_id)
                        VALUES (?, ?)
                        """,
                        film.getId(),
                        genre.getId()
                );
            }
        }
    }

    private void saveLikes(Film film) {
        jdbcTemplate.update(
                "DELETE FROM film_likes WHERE film_id = ?",
                film.getId()
        );

        if (film.getLikes() == null) {
            return;
        }

        Set<Integer> uniqueLikes = new HashSet<>(film.getLikes());

        for (Integer userId : uniqueLikes) {
            jdbcTemplate.update(
                    """
                    INSERT INTO film_likes (film_id, user_id)
                    VALUES (?, ?)
                    """,
                    film.getId(),
                    userId
            );
        }
    }
}
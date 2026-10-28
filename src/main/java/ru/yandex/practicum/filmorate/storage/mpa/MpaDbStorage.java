package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository("mpaDbStorage")
public class MpaDbStorage implements MpaStorage {

    private static final String FIND_ALL_MPA_QUERY =
            "SELECT id, name FROM mpa_ratings ORDER BY id";

    private static final String FIND_MPA_BY_ID_QUERY =
            "SELECT id, name FROM mpa_ratings WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> getAll() {
        return jdbcTemplate.query(
                FIND_ALL_MPA_QUERY,
                (rs, rowNum) -> {
                    Mpa mpa = new Mpa();
                    mpa.setId(rs.getInt("id"));
                    mpa.setName(rs.getString("name"));
                    return mpa;
                }
        );
    }

    @Override
    public Mpa getById(int id) {
        try {
            return jdbcTemplate.queryForObject(
                    FIND_MPA_BY_ID_QUERY,
                    (rs, rowNum) -> {
                        Mpa mpa = new Mpa();
                        mpa.setId(rs.getInt("id"));
                        mpa.setName(rs.getString("name"));
                        return mpa;
                    },
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(
                    "Рейтинг MPA с id=" + id + " не найден"
            );
        }
    }
}
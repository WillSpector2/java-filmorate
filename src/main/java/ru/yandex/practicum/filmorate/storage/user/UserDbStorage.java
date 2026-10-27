package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {

    private static final String INSERT_USER_QUERY = """
            INSERT INTO users (email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_USER_QUERY = """
            UPDATE users
            SET email = ?, login = ?, name = ?, birthday = ?
            WHERE id = ?
            """;

    private static final String FIND_USER_BY_ID_QUERY = """
            SELECT id, email, login, name, birthday
            FROM users
            WHERE id = ?
            """;

    private static final String FIND_ALL_USER_IDS_QUERY =
            "SELECT id FROM users ORDER BY id";

    private static final String DELETE_USER_QUERY =
            "DELETE FROM users WHERE id = ?";

    private static final String FIND_FRIEND_IDS_QUERY = """
            SELECT friend_id
            FROM friendships
            WHERE user_id = ?
            ORDER BY friend_id
            """;

    private static final String FIND_FRIEND_IDS_FOR_UPDATE_QUERY =
            "SELECT friend_id FROM friendships WHERE user_id = ?";

    private static final String DELETE_USER_FRIENDSHIPS_QUERY =
            "DELETE FROM friendships WHERE user_id = ?";

    private static final String INSERT_FRIENDSHIP_QUERY = """
            INSERT INTO friendships (user_id, friend_id)
            VALUES (?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public User add(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    INSERT_USER_QUERY,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());

            if (user.getBirthday() == null) {
                statement.setDate(4, null);
            } else {
                statement.setDate(4, Date.valueOf(user.getBirthday()));
            }

            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException(
                    "Не удалось получить id созданного пользователя"
            );
        }

        user.setId(generatedId.intValue());

        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
            saveFriends(user);
        }

        return getById(user.getId());
    }

    @Override
    @Transactional
    public User update(User user) {
        User existingUser = getById(user.getId());

        int updatedRows = jdbcTemplate.update(
                UPDATE_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() == null
                        ? null
                        : Date.valueOf(user.getBirthday()),
                user.getId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException(
                    "User with id=" + user.getId() + " not found"
            );
        }

        if (user.getFriends() == null) {
            user.setFriends(existingUser.getFriends());
        }

        saveFriends(user);

        return getById(user.getId());
    }

    @Override
    public User getById(int id) {
        User user;

        try {
            user = jdbcTemplate.queryForObject(
                    FIND_USER_BY_ID_QUERY,
                    (resultSet, rowNum) -> {
                        User result = new User();

                        result.setId(resultSet.getInt("id"));
                        result.setEmail(resultSet.getString("email"));
                        result.setLogin(resultSet.getString("login"));
                        result.setName(resultSet.getString("name"));

                        Date birthday = resultSet.getDate("birthday");
                        result.setBirthday(
                                birthday == null
                                        ? null
                                        : birthday.toLocalDate()
                        );

                        return result;
                    },
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(
                    "User with id=" + id + " not found"
            );
        }

        List<Integer> friendIds = jdbcTemplate.query(
                FIND_FRIEND_IDS_QUERY,
                (resultSet, rowNum) -> resultSet.getInt("friend_id"),
                id
        );

        user.setFriends(new HashSet<>(friendIds));

        return user;
    }

    @Override
    public List<User> getAll() {
        List<Integer> userIds = jdbcTemplate.query(
                FIND_ALL_USER_IDS_QUERY,
                (resultSet, rowNum) -> resultSet.getInt("id")
        );

        List<User> users = new ArrayList<>();

        for (Integer userId : userIds) {
            users.add(getById(userId));
        }

        return users;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        return jdbcTemplate.update(DELETE_USER_QUERY, id) > 0;
    }

    private void saveFriends(User user) {
        int userId = user.getId();

        Set<Integer> newFriendIds = user.getFriends() == null
                ? new HashSet<>()
                : new HashSet<>(user.getFriends());

        jdbcTemplate.update(
                DELETE_USER_FRIENDSHIPS_QUERY,
                userId
        );

        for (Integer friendId : newFriendIds) {
            jdbcTemplate.update(
                    INSERT_FRIENDSHIP_QUERY,
                    userId,
                    friendId
            );
        }
    }
}
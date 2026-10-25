package ru.yandex.practicum.filmorate.storage.user;

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

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public User add(User user) {
        String sql = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());

            if (user.getBirthday() == null) {
                statement.setDate(4, null);
            } else {
                statement.setDate(
                        4, Date.valueOf(user.getBirthday())
                );
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
        // Получаем текущие данные до изменения профиля.
        User existingUser = getById(user.getId());

        String sql = """
                UPDATE users
                SET email = ?, login = ?, name = ?, birthday = ?
                WHERE id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
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

        // Если список друзей не передан, сохраняем существующие связи.
        if (user.getFriends() == null) {
            user.setFriends(existingUser.getFriends());
        }

        saveFriends(user);

        return getById(user.getId());
    }

    @Override
    public User getById(int id) {
        List<User> users = jdbcTemplate.query(
                """
                SELECT id, email, login, name, birthday
                FROM users
                WHERE id = ?
                """,
                (resultSet, rowNum) -> {
                    User user = new User();

                    user.setId(resultSet.getInt("id"));
                    user.setEmail(resultSet.getString("email"));
                    user.setLogin(resultSet.getString("login"));
                    user.setName(resultSet.getString("name"));

                    Date birthday = resultSet.getDate("birthday");
                    user.setBirthday(
                            birthday == null ? null : birthday.toLocalDate()
                    );

                    return user;
                },
                id
        );

        if (users.isEmpty()) {
            throw new NotFoundException(
                    "User with id=" + id + " not found"
            );
        }

        User user = users.get(0);

        List<Integer> friendIds = jdbcTemplate.query(
                """
                SELECT friend_id
                FROM friendships
                WHERE user_id = ?
                ORDER BY friend_id
                """,
                (resultSet, rowNum) -> resultSet.getInt("friend_id"),
                id
        );

        user.setFriends(new HashSet<>(friendIds));

        return user;
    }

    @Override
    public List<User> getAll() {
        List<Integer> userIds = jdbcTemplate.query(
                "SELECT id FROM users ORDER BY id",
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
        return jdbcTemplate.update(
                "DELETE FROM users WHERE id = ?",
                id
        ) > 0;
    }

    private void saveFriends(User user) {
        int userId = user.getId();

        Set<Integer> oldFriendIds = new HashSet<>(
                jdbcTemplate.query(
                        "SELECT friend_id FROM friendships WHERE user_id = ?",
                        (resultSet, rowNum) ->
                                resultSet.getInt("friend_id"),
                        userId
                )
        );

        Set<Integer> newFriendIds = user.getFriends() == null
                ? new HashSet<>()
                : new HashSet<>(user.getFriends());

        // Удалённая исходящая связь переводит обратную заявку
        // в неподтверждённое состояние.
        for (Integer oldFriendId : oldFriendIds) {
            if (!newFriendIds.contains(oldFriendId)) {
                jdbcTemplate.update(
                        """
                        UPDATE friendships
                        SET status = 'UNCONFIRMED'
                        WHERE user_id = ? AND friend_id = ?
                        """,
                        oldFriendId,
                        userId
                );
            }
        }

        // Пересоздаём исходящие связи пользователя.
        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ?",
                userId
        );

        for (Integer friendId : newFriendIds) {
            Integer reciprocalCount = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM friendships
                    WHERE user_id = ? AND friend_id = ?
                    """,
                    Integer.class,
                    friendId,
                    userId
            );

            boolean confirmed = reciprocalCount != null
                    && reciprocalCount > 0;

            String status = confirmed
                    ? "CONFIRMED"
                    : "UNCONFIRMED";

            jdbcTemplate.update(
                    """
                    INSERT INTO friendships (user_id, friend_id, status)
                    VALUES (?, ?, ?)
                    """,
                    userId,
                    friendId,
                    status
            );

            if (confirmed) {
                jdbcTemplate.update(
                        """
                        UPDATE friendships
                        SET status = 'CONFIRMED'
                        WHERE user_id = ? AND friend_id = ?
                        """,
                        friendId,
                        userId
                );
            }
        }
    }
}
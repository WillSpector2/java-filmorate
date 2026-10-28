package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Autowired
    UserDbStorageTest(UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Test
    void shouldAddAndFindUserById() {
        User user = createUser("first@example.com", "first");

        User savedUser = userStorage.add(user);

        assertTrue(savedUser.getId() > 0);
        assertEquals("first@example.com", savedUser.getEmail());
        assertEquals("first", savedUser.getLogin());

        User foundUser = userStorage.getById(savedUser.getId());

        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(savedUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void shouldReturnAllUsers() {
        userStorage.add(createUser("first@example.com", "first"));
        userStorage.add(createUser("second@example.com", "second"));

        assertEquals(2, userStorage.getAll().size());
    }

    @Test
    void shouldUpdateUser() {
        User user = userStorage.add(
                createUser("first@example.com", "first")
        );

        user.setName("Updated Name");
        user.setLogin("updated");

        User updatedUser = userStorage.update(user);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated", updatedUser.getLogin());
    }

    @Test
    void shouldDeleteUser() {
        User user = userStorage.add(
                createUser("first@example.com", "first")
        );

        assertTrue(userStorage.delete(user.getId()));
        assertTrue(userStorage.getAll().isEmpty());
    }

    @Test
    void shouldStoreFriendsAsOneWayRelationships() {
        User first = userStorage.add(
                createUser("first@example.com", "first")
        );

        User second = userStorage.add(
                createUser("second@example.com", "second")
        );

        first.setFriends(Set.of(second.getId()));
        userStorage.update(first);

        User firstFromDb = userStorage.getById(first.getId());
        User secondFromDb = userStorage.getById(second.getId());

        assertTrue(firstFromDb.getFriends().contains(second.getId()));
        assertFalse(secondFromDb.getFriends().contains(first.getId()));
    }

    @Test
    void shouldReturnCommonFriends() {
        User first = userStorage.add(
                createUser("first@example.com", "first")
        );
        User second = userStorage.add(
                createUser("second@example.com", "second")
        );
        User commonFriend = userStorage.add(
                createUser("friend@example.com", "friend")
        );

        first.setFriends(Set.of(commonFriend.getId()));
        second.setFriends(Set.of(commonFriend.getId()));

        userStorage.update(first);
        userStorage.update(second);

        List<User> commonFriends = userStorage.getCommonFriends(
                first.getId(),
                second.getId()
        );

        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.get(0).getId());
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoCommonFriends() {
        User first = userStorage.add(
                createUser("first@example.com", "first")
        );
        User second = userStorage.add(
                createUser("second@example.com", "second")
        );
        User firstFriend = userStorage.add(
                createUser("friend1@example.com", "friend1")
        );
        User secondFriend = userStorage.add(
                createUser("friend2@example.com", "friend2")
        );

        first.setFriends(Set.of(firstFriend.getId()));
        second.setFriends(Set.of(secondFriend.getId()));

        userStorage.update(first);
        userStorage.update(second);

        List<User> commonFriends = userStorage.getCommonFriends(
                first.getId(),
                second.getId()
        );

        assertTrue(commonFriends.isEmpty());
    }

    @Test
    void shouldReturnAllCommonFriends() {
        User first = userStorage.add(
                createUser("first@example.com", "first")
        );
        User second = userStorage.add(
                createUser("second@example.com", "second")
        );
        User friend1 = userStorage.add(
                createUser("friend1@example.com", "friend1")
        );
        User friend2 = userStorage.add(
                createUser("friend2@example.com", "friend2")
        );
        User onlyFirstFriend = userStorage.add(
                createUser("onlyfirst@example.com", "onlyfirst")
        );

        first.setFriends(Set.of(
                friend1.getId(),
                friend2.getId(),
                onlyFirstFriend.getId()
        ));
        second.setFriends(Set.of(
                friend1.getId(),
                friend2.getId()
        ));

        userStorage.update(first);
        userStorage.update(second);

        List<User> commonFriends = userStorage.getCommonFriends(
                first.getId(),
                second.getId()
        );

        assertEquals(2, commonFriends.size());
        assertTrue(commonFriends.stream()
                .anyMatch(user -> user.getId() == friend1.getId()));
        assertTrue(commonFriends.stream()
                .anyMatch(user -> user.getId() == friend2.getId()));
    }

    private User createUser(String email, String login) {
        User user = new User();

        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        return user;
    }
}
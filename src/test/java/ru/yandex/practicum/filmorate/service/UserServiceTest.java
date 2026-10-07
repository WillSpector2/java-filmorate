package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    private UserStorage userStorage;
    private UserService userService;

    private User firstUser;
    private User secondUser;
    private User thirdUser;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);

        firstUser = createUser("first@example.com", "first");
        secondUser = createUser("second@example.com", "second");
        thirdUser = createUser("third@example.com", "third");

        userStorage.add(firstUser);
        userStorage.add(secondUser);
        userStorage.add(thirdUser);
    }

    @Test
    void shouldAddFriendForBothUsers() {
        userService.addFriend(
                firstUser.getId(),
                secondUser.getId()
        );

        assertTrue(firstUser.getFriends().contains(secondUser.getId()));
        assertTrue(secondUser.getFriends().contains(firstUser.getId()));
    }

    @Test
    void shouldRemoveFriendForBothUsers() {
        userService.addFriend(
                firstUser.getId(),
                secondUser.getId()
        );

        userService.removeFriend(
                firstUser.getId(),
                secondUser.getId()
        );

        assertFalse(firstUser.getFriends().contains(secondUser.getId()));
        assertFalse(secondUser.getFriends().contains(firstUser.getId()));
    }

    @Test
    void shouldReturnFriends() {
        userService.addFriend(
                firstUser.getId(),
                secondUser.getId()
        );

        List<User> friends = userService.getFriends(firstUser.getId());

        assertEquals(1, friends.size());
        assertEquals(secondUser.getId(), friends.get(0).getId());
    }

    @Test
    void shouldReturnCommonFriends() {
        userService.addFriend(
                firstUser.getId(),
                secondUser.getId()
        );

        userService.addFriend(
                firstUser.getId(),
                thirdUser.getId()
        );

        userService.addFriend(
                secondUser.getId(),
                thirdUser.getId()
        );

        List<User> commonFriends = userService.getCommonFriends(
                firstUser.getId(),
                secondUser.getId()
        );

        assertEquals(1, commonFriends.size());
        assertEquals(thirdUser.getId(), commonFriends.get(0).getId());
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

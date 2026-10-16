package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    void shouldCreateUnconfirmedFriendRequest() {
        Friendship friendship = userService.addFriend(
                firstUser.getId(),
                secondUser.getId()
        );

        assertNotNull(friendship);

        User updatedFirstUser = userStorage.getById(firstUser.getId());
        assertTrue(updatedFirstUser.getFriends().contains(secondUser.getId()));

        assertEquals(1, userService.getFriends(firstUser.getId()).size());
    }

    @Test
    void shouldConfirmFriendshipWhenRequestIsReciprocated() {
        userService.addFriend(
                firstUser.getId(),
                secondUser.getId()
        );

        userService.addFriend(
                secondUser.getId(),
                firstUser.getId()
        );

        User updatedFirstUser = userStorage.getById(firstUser.getId());
        User updatedSecondUser = userStorage.getById(secondUser.getId());

        assertTrue(updatedFirstUser.getFriends().contains(secondUser.getId()));
        assertTrue(updatedSecondUser.getFriends().contains(firstUser.getId()));

        assertEquals(1, userService.getFriends(firstUser.getId()).size());
        assertEquals(1, userService.getFriends(secondUser.getId()).size());
    }

    @Test
    void shouldRemoveFriendForBothUsers() {
        userService.addFriend(firstUser.getId(), secondUser.getId());
        userService.addFriend(secondUser.getId(), firstUser.getId());

        userService.removeFriend(
                firstUser.getId(),
                secondUser.getId()
        );

        User updatedFirstUser = userStorage.getById(firstUser.getId());
        User updatedSecondUser = userStorage.getById(secondUser.getId());

        assertFalse(updatedFirstUser.getFriends().contains(secondUser.getId()));
        assertTrue(userService.getFriends(firstUser.getId()).isEmpty());

        assertTrue(updatedSecondUser.getFriends().contains(firstUser.getId()));
        assertEquals(1, userService.getFriends(secondUser.getId()).size());
    }

    @Test
    void shouldReturnOnlyConfirmedFriends() {
        userService.addFriend(
                firstUser.getId(),
                secondUser.getId()
        );

        userService.addFriend(
                firstUser.getId(),
                thirdUser.getId()
        );

        List<User> friends = userService.getFriends(firstUser.getId());

        assertEquals(2, friends.size());
    }

    @Test
    void shouldReturnCommonConfirmedFriends() {
        userService.addFriend(firstUser.getId(), thirdUser.getId());
        userService.addFriend(secondUser.getId(), thirdUser.getId());

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
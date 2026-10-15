package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
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

        assertEquals(FriendshipStatus.UNCONFIRMED, friendship.getStatus());
        assertTrue(firstUser.getFriends().contains(secondUser.getId()));
        assertFalse(secondUser.getFriends().contains(firstUser.getId()));

        // Неподтверждённый запрос ещё не считается дружбой.
        assertTrue(userService.getFriends(firstUser.getId()).isEmpty());
    }

    @Test
    void shouldConfirmFriendshipWhenRequestIsReciprocated() {
        userService.addFriend(
                firstUser.getId(),
                secondUser.getId()
        );

        Friendship friendship = userService.addFriend(
                secondUser.getId(),
                firstUser.getId()
        );

        assertEquals(FriendshipStatus.CONFIRMED, friendship.getStatus());
        assertTrue(firstUser.getFriends().contains(secondUser.getId()));
        assertTrue(secondUser.getFriends().contains(firstUser.getId()));

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

        assertFalse(firstUser.getFriends().contains(secondUser.getId()));
        assertFalse(secondUser.getFriends().contains(firstUser.getId()));
        assertTrue(userService.getFriends(firstUser.getId()).isEmpty());
    }

    @Test
    void shouldReturnOnlyConfirmedFriends() {
        userService.addFriend(
                firstUser.getId(),
                secondUser.getId()
        );

        userService.addFriend(
                thirdUser.getId(),
                firstUser.getId()
        );

        userService.addFriend(
                firstUser.getId(),
                thirdUser.getId()
        );

        List<User> friends = userService.getFriends(firstUser.getId());

        assertEquals(1, friends.size());
        assertEquals(thirdUser.getId(), friends.get(0).getId());
    }

    @Test
    void shouldReturnCommonConfirmedFriends() {
        // Подтверждаем дружбу первого и третьего пользователей.
        userService.addFriend(firstUser.getId(), thirdUser.getId());
        userService.addFriend(thirdUser.getId(), firstUser.getId());

        // Подтверждаем дружбу второго и третьего пользователей.
        userService.addFriend(secondUser.getId(), thirdUser.getId());
        userService.addFriend(thirdUser.getId(), secondUser.getId());

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
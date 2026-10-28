package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage
    ) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        return userStorage.add(user);
    }

    public User updateUser(User user) {
        User existingUser = userStorage.getById(user.getId());

        user.setFriends(existingUser.getFriends());

        return userStorage.update(user);
    }

    public List<User> getUsers() {
        return userStorage.getAll();
    }

    public User getUserById(int id) {
        return userStorage.getById(id);
    }

    public Friendship addFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        user.getFriends().add(friendId);
        userStorage.update(user);

        boolean incomingRequestExists = friend.getFriends().contains(userId);

        Friendship friendship = new Friendship();
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);

        if (incomingRequestExists) {
            friendship.setStatus(FriendshipStatus.CONFIRMED);
        } else {
            friendship.setStatus(FriendshipStatus.UNCONFIRMED);
        }

        return friendship;
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        userStorage.getById(friendId);

        user.getFriends().remove(friendId);

        userStorage.update(user);
    }

    public List<User> getFriends(int userId) {
        User user = userStorage.getById(userId);
        List<User> friends = new ArrayList<>();

        for (Integer friendId : user.getFriends()) {
            friends.add(userStorage.getById(friendId));
        }

        return friends;
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        userStorage.getById(userId);
        userStorage.getById(otherId);

        return userStorage.getCommonFriends(userId, otherId);
    }
}
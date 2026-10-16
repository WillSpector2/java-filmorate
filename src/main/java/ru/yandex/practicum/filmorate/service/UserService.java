package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        return userStorage.add(user);
    }

    public User updateUser(User user) {
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

        // Проверяем, отправлял ли второй пользователь запрос первым.
        boolean incomingRequestExists = friend.getFriends().contains(userId);

        // Добавляем друга в список инициатора запроса.
        user.getFriends().add(friendId);
        userStorage.update(user);

        Friendship friendship = new Friendship();
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);

        if (incomingRequestExists) {
            // Встречный запрос подтверждает дружбу.
            friend.getFriends().add(userId);
            userStorage.update(friend);

            friendship.setStatus(FriendshipStatus.CONFIRMED);
        } else {
            // Первый запрос пока не подтверждён.
            friendship.setStatus(FriendshipStatus.UNCONFIRMED);
        }

        return friendship;
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        // Удаляем связь с обеих сторон.
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public List<User> getFriends(int userId) {
        User user = userStorage.getById(userId);
        List<User> friends = new ArrayList<>();

        // Возвращаем только взаимно подтверждённых друзей.
        for (Integer friendId : user.getFriends()) {
            User friend = userStorage.getById(friendId);

            if (friend.getFriends().contains(userId)) {
                friends.add(friend);
            }
        }

        return friends;
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        List<User> userFriends = getFriends(userId);
        List<User> otherFriends = getFriends(otherId);

        Set<Integer> commonFriendIds = new HashSet<>();

        for (User friend : userFriends) {
            commonFriendIds.add(friend.getId());
        }

        Set<Integer> otherFriendIds = new HashSet<>();

        for (User friend : otherFriends) {
            otherFriendIds.add(friend.getId());
        }

        // Оставляем только общих друзей.
        commonFriendIds.retainAll(otherFriendIds);

        List<User> commonFriends = new ArrayList<>();

        for (Integer friendId : commonFriendIds) {
            commonFriends.add(userStorage.getById(friendId));
        }

        return commonFriends;
    }
}
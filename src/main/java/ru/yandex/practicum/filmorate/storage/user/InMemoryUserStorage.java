package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final List<User> users = new ArrayList<>();

    @Override
    public User add(User user) {
        user.setId(getNextId());
        users.add(user);
        return user;
    }

    @Override
    public User update(User user) {
        User existingUser = getById(user.getId());
        users.set(users.indexOf(existingUser), user);
        return user;
    }

    @Override
    public User getById(int id) {
        return users.stream()
                .filter(user -> user.getId() == id)
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException(
                                "User with id=" + id + " not found"
                        )
                );
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users);
    }

    @Override
    public boolean delete(int id) {
        User user = getById(id);
        return users.remove(user);
    }

    private int getNextId() {
        return users.stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0) + 1;
    }
}
package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        validateUser(user);

        setDefaultName(user);

        User createdUser = userService.createUser(user);

        log.info("Добавлен пользователь: {}", createdUser);

        return createdUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        validateUser(user);

        setDefaultName(user);

        User updatedUser = userService.updateUser(user);

        log.info(
                "Обновлён пользователь с id={}: {}",
                updatedUser.getId(),
                updatedUser
        );

        return updatedUser;
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable int id,
            @PathVariable int friendId) {

        userService.addFriend(id, friendId);

        log.info(
                "Пользователи с id={} и id={} добавлены в друзья",
                id,
                friendId
        );
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(
            @PathVariable int id,
            @PathVariable int friendId) {

        userService.removeFriend(id, friendId);

        log.info(
                "Пользователи с id={} и id={} удалены из друзей",
                id,
                friendId
        );
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(
            @PathVariable int id,
            @PathVariable int otherId) {

        return userService.getCommonFriends(id, otherId);
    }

    private void validateUser(User user) {
        if (user.getBirthday() != null
                && user.getBirthday().isAfter(LocalDate.now())) {

            log.error(
                    "Дата рождения пользователя {} находится в будущем",
                    user.getLogin()
            );

            throw new ValidationException(
                    "Дата рождения не может быть в будущем"
            );
        }

        if (user.getLogin() != null
                && user.getLogin().contains(" ")) {

            log.error(
                    "Логин пользователя содержит пробел: {}",
                    user.getLogin()
            );

            throw new ValidationException(
                    "Логин не может содержать пробелы"
            );
        }
    }

    private void setDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
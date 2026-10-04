package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users = new ArrayList<>();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        validateUser(user);

        setDefaultName(user);

        user.setId(getNextId());

        users.add(user);

        log.info("Добавлен пользователь: {}", user);

        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        validateUser(user);

        setDefaultName(user);

        for (int i = 0; i < users.size(); i++) {
            User oldUser = users.get(i);

            if (oldUser.getId() == user.getId()) {
                users.set(i, user);

                log.info(
                        "Обновлён пользователь с id={}: {}",
                        user.getId(),
                        user
                );

                return user;
            }
        }

        log.error("Пользователь с id={} не найден", user.getId());

        throw new ValidationException(
                "Пользователь с id=" + user.getId() + " не найден"
        );
    }

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users);
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

    private int getNextId() {
        return users.stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0) + 1;
    }
}
package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    @Test
    void shouldCreateUser() {
        User user = createUser();

        User created = controller.createUser(user);

        assertEquals(1, created.getId());
        assertEquals(1, controller.getUsers().size());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        User user = createUser();
        user.setName("");

        User created = controller.createUser(user);

        assertEquals(user.getLogin(), created.getName());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        User user = createUser();
        user.setName(null);

        User created = controller.createUser(user);

        assertEquals(user.getLogin(), created.getName());
    }

    @Test
    void shouldRejectLoginWithSpaces() {
        User user = createUser();
        user.setLogin("lock john");

        assertThrows(
                ValidationException.class,
                () -> controller.createUser(user)
        );
    }

    @Test
    void shouldUpdateExistingUser() {
        User user = createUser();
        controller.createUser(user);

        user.setName("New Name");

        User updated = controller.updateUser(user);

        assertEquals("New Name", updated.getName());
        assertEquals(1, controller.getUsers().size());
    }

    @Test
    void shouldRejectUpdateOfUnknownUser() {
        User user = createUser();
        user.setId(999);

        assertThrows(
                NotFoundException.class,
                () -> controller.updateUser(user)
        );
    }

    private User createUser() {
        User user = new User();

        user.setEmail("user@example.com");
        user.setLogin("lock");
        user.setName("Lock");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        return user;
    }
}
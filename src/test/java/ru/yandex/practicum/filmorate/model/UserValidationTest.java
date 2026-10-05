package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldRejectBlankEmail() {
        User user = createValidUser();
        user.setEmail("");

        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldRejectInvalidEmail() {
        User user = createValidUser();
        user.setEmail("invalid-email");

        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldRejectEmptyLogin() {
        User user = createValidUser();
        user.setLogin("");

        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldAcceptEmptyName() {
        User user = createValidUser();
        user.setName("");

        assertTrue(validator.validate(user).isEmpty());
    }

    @Test
    void shouldRejectNullBirthday() {
        User user = createValidUser();
        user.setBirthday(null);

        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldRejectFutureBirthday() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertFalse(validator.validate(user).isEmpty());
    }

    private User createValidUser() {
        User user = new User();

        user.setEmail("user@example.com");
        user.setLogin("user");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        return user;
    }
}

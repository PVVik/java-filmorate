package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.Month;

public class UserControllerTest {
    private final UserController controller = new UserController();
    private final User user = User.builder()
            .id(1)
            .email("a@yandex.ru")
            .login("login")
            .name("name")
            .birthday(LocalDate.of(1997, Month.MARCH, 25))
            .build();

    @Test
    void shouldCreateAUser() throws ValidationException {
        User thisUser = new User(1, "a@yandex.ru", "login", "name",
                LocalDate.of(1997, Month.MARCH, 25));
        controller.addUser(thisUser);

        Assertions.assertEquals(user, thisUser);
        Assertions.assertEquals(1, controller.getAllUsers().size());
    }

    @Test
    void shouldUpdateUser() throws ValidationException {
        User thisUser = new User(1, "b@yandex.ru", "login", "name",
                LocalDate.of(2000, Month.MARCH, 25));
        controller.addUser(user);
        controller.updateUser(thisUser);

        Assertions.assertEquals("b@yandex.ru", thisUser.getEmail());
        Assertions.assertEquals(user.getId(), thisUser.getId());
        Assertions.assertEquals(1, controller.getAllUsers().size());
    }

    @Test
    void shouldCreateAUserIfNameIsEmpty() throws ValidationException {
        User thisUser = new User(1, "a@yandex.ru", "login", "",
                LocalDate.of(1997, Month.MARCH, 25));
        controller.addUser(thisUser);

        Assertions.assertEquals(1, thisUser.getId());
        Assertions.assertEquals("login", thisUser.getName());
    }

    @Test
    void shouldThrowExceptionIfEmailIncorrect() {
        user.setEmail("a.yandex.ru");

        Assertions.assertThrows(ValidationException.class, () -> controller.addUser(user));
        Assertions.assertEquals(0, controller.getAllUsers().size());
    }

    @Test
    void shouldThrowExceptionIfEmailIsEmpty() {
        user.setEmail("");

        Assertions.assertThrows(ValidationException.class, () -> controller.addUser(user));
        Assertions.assertEquals(0, controller.getAllUsers().size());
    }

    @Test
    void shouldNotAddUserIfLoginIsEmpty() {
        user.setLogin("");

        Assertions.assertThrows(ValidationException.class, () -> controller.addUser(user));
        Assertions.assertEquals(0, controller.getAllUsers().size());
    }

    @Test
    void shouldNotAddUserIfBirthdayIsInTheFuture() {
        user.setBirthday(LocalDate.of(2024, Month.MARCH, 25));

        Assertions.assertThrows(ValidationException.class, () -> controller.addUser(user));
        Assertions.assertEquals(0, controller.getAllUsers().size());
    }

}
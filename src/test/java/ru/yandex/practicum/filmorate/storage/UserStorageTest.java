package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Set;

public class UserStorageTest {

    private UserStorage userStorage;
    private final User rightUser = User.builder().email("email@email.ru").login("login").name("name")
            .birthday(LocalDate.of(2000, Month.APRIL, 22)).build();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @BeforeEach
    public void beforeEach() {
        userStorage = new InMemoryUserStorage();
    }

    @Test
    @DisplayName("Метод должен создать пользователя с корректными данными")
    public void addUser_shouldAddUser() {
        User addedUser = userStorage.addUser(rightUser);

        Assertions.assertEquals(1, addedUser.getId());
        Assertions.assertEquals(rightUser, addedUser);
    }

    @Test
    @DisplayName("Метод должен выдать ошибку из-за некорректного email")
    public void addUser_shouldGetErrorWithWrongEmail() {
        User user = User.builder().email("  ").login("login").build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("Передан некорректный email", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Метод должен выдать ошибку из-за пустого email")
    public void addUser_shouldGetErrorWithEmptyEmail() {
        User user = User.builder().login("login").build();

        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
    }

    @Test
    @DisplayName("Метод должен выдать ошибку из-за некорректного логина")
    public void addUser_shouldGetErrorWithWrongLogin() {
        User user = User.builder().email("email@email.ru").login("  ").build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("Логин не может содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Метод должен выдать ошибку из-за пустого логина")
    public void addUser_shouldGetErrorWithEmptyLogin() {
        User user = User.builder().email("email@email.ru")
                .birthday(LocalDate.of(1999, Month.FEBRUARY, 11)).build();

        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
    }

    @Test
    @DisplayName("Метод должен выдать ошибку из-за некорректной даты рождения")
    public void addUser_shouldGetErrorWithWrongBirthday() {
        User user = User.builder().email("email@email.ru").login("login")
                .birthday(LocalDate.of(3000, Month.FEBRUARY, 11)).build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("Передана некорректная дата рождения", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Метод должен выдать ошибку из-за пустой даты рождения")
    public void addUser_shouldGetErrorWithEmptyBirthday() {
        User user = User.builder().email("email@email.ru").login("login").build();

        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
    }

    @Test
    @DisplayName("Метод должен подставить логин вместо имени, если имя не передано")
    public void addUser_shouldUseLoginIfEmptyName() {
        User user = User.builder().email("email@email.ru").login("login")
                .birthday(LocalDate.of(1997, Month.MARCH, 25)).build();

        Assertions.assertEquals(User.builder().id(1).email("email@email.ru").login("login").name("login")
                .birthday(LocalDate.of(1997, Month.MARCH, 25)).build(), userStorage.addUser(user));
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку при дублировании email")
    public void addUser_shouldGetErrorWithRepeatEmail() {
        userStorage.addUser(rightUser);

        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(rightUser));
    }

    @Test
    @DisplayName("Метод должен успешно обновить email")
    public void updateUser_shouldUpdateEmail() {
        userStorage.addUser(rightUser);
        User user = userStorage.updateUser(User.builder().id(1).email("newEmail@email.ru").build());

        Assertions.assertEquals("newEmail@email.ru", user.getEmail());
    }

    @Test
    @DisplayName("Метод должен успешно обновить логин")
    public void updateUser_shouldUpdateLogin() {
        userStorage.addUser(rightUser);
        User user = userStorage.updateUser(User.builder().id(1).login("newLogin").build());

        Assertions.assertEquals("newLogin", user.getLogin());
    }

    @Test
    @DisplayName("Метод должен успешно обновить имя")
    public void updateUser_shouldUpdateName() {
        userStorage.addUser(rightUser);
        User user = userStorage.updateUser(User.builder().id(1).name("newName").build());

        Assertions.assertEquals("newName", user.getName());
    }

    @Test
    @DisplayName("Метод должен успешно обновить дату рождения")
    public void updateUser_shouldUpdateBirthday() {
        userStorage.addUser(rightUser);
        User user = userStorage.updateUser(User.builder().id(1)
                .birthday(LocalDate.of(1957, Month.MAY, 27)).build());

        Assertions.assertEquals(LocalDate.of(1957, Month.MAY, 27), user.getBirthday());
    }

    @Test
    @DisplayName("Метод должен вернуть пустой список, если нет созданных пользователей")
    public void getUsers_shouldGetEmptyList() {
        Assertions.assertEquals(new ArrayList<>(), userStorage.getUsers());
    }

    @Test
    @DisplayName("Метод должен вернуть корректный список")
    public void getUsers_shouldGetCorrectList() {
        userStorage.addUser(rightUser);

        Assertions.assertEquals(1, userStorage.getUsers().size());
    }
}

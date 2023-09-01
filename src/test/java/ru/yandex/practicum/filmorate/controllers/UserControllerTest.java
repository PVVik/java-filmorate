package ru.yandex.practicum.filmorate.controllers;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

public class UserControllerTest {
    private InMemoryUserStorage storage = new InMemoryUserStorage();
    private UserService service = new UserService(storage);
    private UserController controller = new UserController(service);
    private final User user = new User(1, "lollipop@yandex.ru", "scramble", "Matthew",
            LocalDate.of(1990, 1, 1), new HashSet<>());
    private final User updatedUser = new User(1, "yandex@yandex.ru", "scramble", "Matthew",
            LocalDate.of(1990, 2, 1), new HashSet<>());
    private final User emptyNameUser = new User(6, "mail@yandex.ru", "user", null,
            LocalDate.of(1990, 1, 1), new HashSet<>());
    private final User incorrectEmailUser = new User(3, "somebody once told me, the world is gonna roll me",
            "awesome guy", "Andrew", LocalDate.of(1997, 8, 13), new HashSet<>());
    private final User emptyEmailUser = new User(1, "", "puss in boots", null,
            LocalDate.of(1990, 1, 1), new HashSet<>());
    private final User commonFriend = new User(19, "friend@yandex.ru", "friend", "Alexander",
            LocalDate.of(1996, 4, 20), new HashSet<>());

    @AfterEach
    public void afterEach() {
        storage.deleteUsers();
    }

    @Test
    void createUser_shouldCreateAUser() throws ValidationException {
        controller.createUser(user);

        Assertions.assertEquals(1, controller.getUsers().size());
    }

    @Test
    void getUserById_shouldReturnUserWithCorrectId() throws ValidationException {
        controller.createUser(user);
        User thisUser = storage.getUserById(user.getId());

        Assertions.assertEquals(thisUser.getId(), user.getId());
    }

    @Test
    void update_shouldUpdateUserData() throws ValidationException {
        controller.createUser(user);
        controller.updateUser(updatedUser);

        Assertions.assertEquals("yandex@yandex.ru", updatedUser.getEmail());
        Assertions.assertEquals(user.getId(), updatedUser.getId());
        Assertions.assertEquals(1, controller.getUsers().size());
    }

    @Test
    void createUser_shouldCreateAUserIfNameIsEmpty() throws ValidationException {
        controller.createUser(emptyNameUser);

        Assertions.assertEquals(6, emptyNameUser.getId());
        Assertions.assertEquals("user", emptyNameUser.getName());
    }

    @Test
    void createUser_shouldThrowExceptionIfEmailIsIncorrect() {
        Assertions.assertThrows(ValidationException.class, () -> controller.createUser(incorrectEmailUser));
        Assertions.assertEquals(0, controller.getUsers().size());
    }

    @Test
    void createUser_shouldThrowExceptionIfEmailIsEmpty() {
        Assertions.assertThrows(ValidationException.class, () -> controller.createUser(emptyEmailUser));
        Assertions.assertEquals(0, controller.getUsers().size());
    }

    @Test
    void createUser_shouldNotAddUserIfLoginIsEmpty() {
        user.setLogin("");

        Assertions.assertThrows(ValidationException.class, () -> controller.createUser(user));
        Assertions.assertEquals(0, controller.getUsers().size());
    }

    @Test
    void createUser_shouldNotAddUserIfBirthdayIsInTheFuture() {
        user.setBirthday(LocalDate.of(2024, 6, 28));

        Assertions.assertThrows(ValidationException.class, () -> controller.createUser(user));
        Assertions.assertEquals(0, controller.getUsers().size());
    }

    @Test
    void addFriend_shouldAddFriendToOtherUsersSet() throws ValidationException {
        controller.createUser(user);
        controller.createUser(emptyNameUser);
        controller.addFriend(user.getId(), emptyNameUser.getId());

        Assertions.assertTrue(user.getFriendsQuantity() != 0);
        Assertions.assertTrue(emptyNameUser.getFriendsQuantity() != 0);
    }

    @Test
    void deleteFriend_shouldDeleteFriendFromOtherUsersSet() throws ValidationException {
        controller.createUser(user);
        controller.createUser(emptyNameUser);
        controller.addFriend(user.getId(), emptyNameUser.getId());
        controller.removeFriend(user.getId(), emptyNameUser.getId());

        Assertions.assertEquals(0, user.getFriendsQuantity());
        Assertions.assertEquals(0, emptyNameUser.getFriendsQuantity());
    }

    @Test
    void getCommonFriends_shouldReturnListWithSizeOne() throws ValidationException {
        controller.createUser(user);
        controller.createUser(emptyNameUser);
        controller.addFriend(user.getId(), emptyNameUser.getId());
        controller.createUser(commonFriend);
        controller.addFriend(user.getId(), commonFriend.getId());
        controller.addFriend(emptyNameUser.getId(), commonFriend.getId());
        List<User> commonFriendList = controller.getCommonFriends(user.getId(), emptyNameUser.getId());

        Assertions.assertEquals(1, commonFriendList.size());
    }

    @Test
    void getFriends_shouldReturnFriendsListOfUser() throws ValidationException {
        controller.createUser(user);
        controller.createUser(emptyNameUser);
        controller.createUser(commonFriend);
        controller.addFriend(user.getId(), emptyNameUser.getId());
        controller.addFriend(user.getId(), commonFriend.getId());
        List<User> listOfUsersFriends = controller.getFriends(user.getId());

        Assertions.assertEquals(2, listOfUsersFriends.size());
    }
}
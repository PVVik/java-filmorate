package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class UserServiceTest {

    private UserService userService;
    private UserStorage userStorage;
    private final User user1 = User.builder().email("email1@email.ru").login("login1").name("name1")
            .birthday(LocalDate.of(2002, Month.MARCH, 1)).build();
    private final User user2 = User.builder().email("email2@email.ru").login("login2").name("name2")
            .birthday(LocalDate.of(2002, Month.MARCH, 2)).build();
    private final User user3 = User.builder().email("email3@email.ru").login("login3").name("name3")
            .birthday(LocalDate.of(2002, Month.MARCH, 3)).build();
    private final User user4 = User.builder().email("email4@email.ru").login("login4").name("name4")
            .birthday(LocalDate.of(2002, Month.MARCH, 4)).build();

    @BeforeEach
    public void beforeEach() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
    }

    @Test
    @DisplayName("Метод должен успешно добавить друзей")
    public void addFriend_shouldAddFriend() {
        userStorage.addUser(user1);
        userStorage.addUser(user2);
        userService.addFriend(user1.getId(), user2.getId());

        Assertions.assertEquals(1, user1.getFriendsIds().size());
        Assertions.assertEquals(1, user2.getFriendsIds().size());
        Assertions.assertTrue(user1.getFriendsIds().contains(2L));
        Assertions.assertTrue(user2.getFriendsIds().contains(1L));
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку, так как пользователь не найден")
    public void addFriend_shouldGetErrorUserNotExists() {
        userStorage.addUser(user1);

        Assertions.assertThrows(NotFoundException.class, () -> userService.addFriend(user1.getId(), 2));
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку, так как id пользователя и друга совпадают")
    public void addFriend_shouldGetErrorUserIdDuplicate() {
        userStorage.addUser(user1);

        Assertions.assertThrows(ValidationException.class, () -> userService.addFriend(user1.getId(), 1));
    }

    @Test
    @DisplayName("Метод должен успешно удалить друзей")
    public void deleteFriend_shouldDeleteFriend() {
        userStorage.addUser(user1);
        userStorage.addUser(user2);
        userService.addFriend(user1.getId(), user2.getId());
        userService.deleteFriend(user2.getId(), user1.getId());

        Assertions.assertEquals(0, user1.getFriendsIds().size());
        Assertions.assertEquals(0, user2.getFriendsIds().size());
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку, так как пользователь не найден")
    public void deleteFriend_shouldGetErrorUserNotExists() {
        userStorage.addUser(user1);

        Assertions.assertThrows(NotFoundException.class, () -> userService.deleteFriend(user1.getId(), 2));
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку, так как id пользователя и друга совпадают")
    public void deleteFriend_shouldGetErrorUserIdDuplicate() {
        userStorage.addUser(user1);

        Assertions.assertThrows(ValidationException.class, () -> userService.deleteFriend(user1.getId(), 1));
    }

    @Test
    @DisplayName("Метод должен вернуть список друзей")
    public void getFriends_shouldGetFriends() {
        userStorage.addUser(user1);
        User friend1 = userStorage.addUser(user2);
        User friend2 = userStorage.addUser(user3);
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());

        List<User> friends = userService.getFriends(user1.getId());

        Assertions.assertEquals(2, friends.size());
        Assertions.assertTrue(friends.contains(friend1));
        Assertions.assertTrue(friends.contains(friend2));
    }

    @Test
    @DisplayName("Метод должен вернуть пустой список")
    public void getFriends_shouldGetEmptyList() {
        userStorage.addUser(user1);

        Assertions.assertEquals(0, userService.getFriends(user1.getId()).size());
    }

    @Test
    @DisplayName("Метод должен вернуть список общих друзей")
    public void getCommonFriends_shouldGetCommonFriends() {
        userStorage.addUser(user1);
        User friend1 = userStorage.addUser(user2);
        User friend2 = userStorage.addUser(user3);
        userStorage.addUser(user4);
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());
        userService.addFriend(user4.getId(), user2.getId());
        userService.addFriend(user4.getId(), user3.getId());

        List<User> friends = userService.getCommonFriends(user1.getId(), user4.getId());

        Assertions.assertEquals(2, friends.size());
        Assertions.assertTrue(friends.contains(friend1));
        Assertions.assertTrue(friends.contains(friend2));
    }

    @Test
    @DisplayName("Метод должен вернуть пустой список")
    public void getCommonFriends_shouldGetEmptyList() {
        userStorage.addUser(user1);
        userStorage.addUser(user2);

        Assertions.assertEquals(0, userService.getCommonFriends(user1.getId(), user2.getId()).size());
    }
}

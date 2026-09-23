package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
public class UserTests {

    private final UserStorage userStorage;
    private final User user = User.builder().email("login@email.ru").login("login").name("name")
            .birthday(LocalDate.of(1999, Month.APRIL, 22)).build();
    private final User otherUser = User.builder().email("login1@email.ru").login("login1").name("name1")
            .birthday(LocalDate.of(1999, Month.APRIL, 23)).build();

    public UserTests(@Autowired UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Test
    @DisplayName("Метод должен успешно создать юзера")
    public void addUser_shouldAddUser() {
        User saved = userStorage.addUser(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("login@email.ru");
        assertThat(saved.getLogin()).isEqualTo("login");
    }

    @Test
    @DisplayName("Метод должен обновить данные юзера")
    public void updateUser_shouldUpdateUser() {
        User saved = userStorage.addUser(user);
        saved.setEmail("new@email.ru");
        saved.setLogin("newlogin");
        saved.setName("New Name");

        userStorage.updateUser(saved);
        User found = userStorage.getUserById(saved.getId());

        assertThat(found.getEmail()).isEqualTo("new@email.ru");
        assertThat(found.getLogin()).isEqualTo("newlogin");
        assertThat(found.getName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("Метод должен вернуть список всех юзеров")
    public void getUsers_shouldReturnAllUsers() {
        userStorage.addUser(user);
        userStorage.addUser(user);

        List<User> users = userStorage.getUsers();

        assertEquals(2, users.size());

    }

    @Test
    @DisplayName("Метод должен найти юзера по id")
    public void getUserById_shouldReturnUser() {
        User saved = userStorage.addUser(user);

        User found = userStorage.getUserById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getLogin()).isEqualTo("login");
    }

    @Test
    @DisplayName("Метод должен бросить NotFoundException для несуществующего id")
    public void getUserById_shouldThrow_whenUserNotFound() {
        assertThatThrownBy(() -> userStorage.getUserById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Метод должен добавить друга")
    public void addFriend_shouldAddFriend() {
        User user1 = userStorage.addUser(user);
        User user2 = userStorage.addUser(otherUser);

        userStorage.addFriend(user1.getId(), user2.getId());

        List<User> friends = userStorage.getFriends(user1.getId());

        assertEquals(1, friends.size());
        assertThat(friends.get(0).getId()).isEqualTo(user2.getId());
    }

    @Test
    @DisplayName("Метод должен удалить друга")
    public void deleteFriend_shouldRemoveFriend() {
        User user1 = userStorage.addUser(user);
        User user2 = userStorage.addUser(otherUser);

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.deleteFriend(user1.getId(), user2.getId());

        List<User> friends = userStorage.getFriends(user1.getId());

        assertEquals(0, friends.size());
    }

    @Test
    @DisplayName("Метод должен вернуть общих друзей")
    public void getCommonFriends_shouldReturnCommonFriends() {
        User user1 = userStorage.addUser(user);
        User user2 = userStorage.addUser(otherUser);
        User common = userStorage.addUser(User.builder().email("login2@email.ru").login("login2").name("name2")
                .birthday(LocalDate.of(1999, Month.APRIL, 24)).build());

        userStorage.addFriend(user1.getId(), common.getId());
        userStorage.addFriend(user2.getId(), common.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertThat(commonFriends.get(0).getId()).isEqualTo(common.getId());
    }
}

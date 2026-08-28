package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString
@Builder
public class User {

    private long id;
    @Email(message = "Передан некорректный email")
    private String email;
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;
    private String name;
    @Past(message = "Передана некорректная дата рождения")
    private LocalDate birthday;
    private Set<Long> friendsIds;

    public void setFriend(long friendId) {
        if (friendId == this.getId()) {
            throw new ValidationException("Пользователь не может добавить с друзья сам себя");
        }
        friendsIds.add(friendId);
    }

    public void deleteFriend(long friendId) {
        if (friendId == this.getId()) {
            throw new ValidationException("Пользователь не может удалить сам себя из друзей");
        }
        friendsIds.remove(friendId);
    }
}

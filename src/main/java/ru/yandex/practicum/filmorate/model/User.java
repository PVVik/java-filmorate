package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;

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
}

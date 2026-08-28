package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString
@Builder
public class Film implements Comparable<Film> {

    private long id;
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;
    @Size(max = 200, message = "Длина описания не может быть больше 200 знаков")
    private String description;
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма может быть только положительной")
    private Long duration;
    private Set<Long> likes;

    public void addLke(long userId) {
        likes.add(userId);
    }

    public void deleteLike(long userId) {
        likes.remove(userId);
    }

    @Override
    public int compareTo(Film o) {
        return o.getLikes().size() - this.getLikes().size();
    }
}

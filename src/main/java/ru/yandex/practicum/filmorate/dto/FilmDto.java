package ru.yandex.practicum.filmorate.dto;

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
@NoArgsConstructor
public class FilmDto {

    private long id;
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;
    @Size(max = 200, message = "Длина описания не может быть больше 200 знаков")
    private String description;
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма может быть только положительной")
    private Long duration;
    private MpaDto mpa;
    private Set<GenreDto> genres;

    public FilmDto(long id, String name, String description, LocalDate releaseDate, Long duration, MpaDto mpa,
                   Set<GenreDto> genres) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        this.genres = genres;
    }

}

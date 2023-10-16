package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    @NotNull
    private Integer id;
    @NotNull
    private String name;

    public Genre(Integer id) {
        this.id = id;
    }
}

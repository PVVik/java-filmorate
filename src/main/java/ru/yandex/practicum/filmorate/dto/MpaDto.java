package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MpaDto {

    private long id;
    private String name;

    public MpaDto(Long id) {
        this.id = id;
    }
}

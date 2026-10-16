package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectorDto {

    private Long id;

    @NotBlank(message = "Укажите имя режиссера")
    private String name;

    public DirectorDto(Long id) {
        this.id = id;
    }
}

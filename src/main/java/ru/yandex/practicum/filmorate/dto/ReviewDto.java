package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long reviewId;

    @NotBlank(message = "Содержание отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва не может быть пустым")
    private Boolean isPositive;

    @NotNull(message = "Пользователь, написавший отзыв, обязателен")
    private Long userId;

    @NotNull(message = "Фильм, к которому пишут отзыв, обязателен")
    private Long filmId;

    private Long useful;
}

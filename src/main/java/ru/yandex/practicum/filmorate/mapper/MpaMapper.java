package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

public class MpaMapper {

    public static MpaDto mapToDto(Mpa mpa) {
        return new MpaDto(mpa.getId(), mpa.getName());
    }
}

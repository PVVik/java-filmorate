package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpaService {

    private final MpaStorage mpaStorage;

    public List<MpaDto> getMpas() {
        log.info("Получили список всех рейтингов");

        return mpaStorage.getMpas().stream().map(MpaMapper::mapToDto).toList();
    }

    public MpaDto getMpaById(long mpaId) {
        log.info("Получили рейтинг с id {}", mpaId);

        return MpaMapper.mapToDto(mpaStorage.getMpaById(mpaId));
    }

    public void checkMpaExists(long mpaId) {
        getMpaById(mpaId);
    }

}

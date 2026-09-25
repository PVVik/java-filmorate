package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;

@Repository
@Slf4j
public class MpaDbStorage extends BaseRepository<Mpa> implements MpaStorage {

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Mpa> getMpas() {
        String query = "SELECT * FROM mpa";

        log.debug("SELECT * FROM mpa");

        return findMany(query);
    }

    @Override
    public Mpa getMpaById(long mpaId) {
        String query = "SELECT * FROM mpa WHERE mpa_id = ?";

        log.debug("SELECT * FROM mpa WHERE mpa_id = {}", mpaId);

        return findOne(query, mpaId).orElseThrow(() ->
                new NotFoundException(String.format("Рейтинг с id %d не найден", mpaId)));
    }

}

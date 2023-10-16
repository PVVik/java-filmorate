package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.db.mpa.MpaDao;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaDbService {
    private final MpaDao mpaDao;

    public Mpa getMpaById(Integer id) {
        if (id == null || !mpaDao.isContains(id)) {
            throw new ObjectNotFoundException("Negative or empty id was passed");
        }
        return mpaDao.getMpaById(id);
    }

    public Collection<Mpa> getMpaList() {
        return mpaDao.getMpaList();
    }
}

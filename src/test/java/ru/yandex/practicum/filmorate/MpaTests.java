package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRowMapper;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaDbStorage.class, MpaRowMapper.class})
class MpaTests {

    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    @DisplayName("Метод должен вернуть список всех MPA-рейтингов")
    public void getMpas_shouldReturnAllMpas() {
        List<Mpa> mpas = mpaStorage.getMpas();

        assertThat(mpas).isNotEmpty();
        assertThat(mpas).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Метод должен найти MPA по ID")
    public void getMpaById_shouldReturnMpa() {
        Mpa mpa = mpaStorage.getMpaById(1);

        assertThat(mpa.getId()).isEqualTo(1);
        assertThat(mpa.getName()).isNotNull();
    }

    @Test
    @DisplayName("Метод должен выбросить NotFoundException для несуществующего ID")
    public void getMpaById_shouldThrow_whenMpaNotFound() {
        assertThatThrownBy(() -> mpaStorage.getMpaById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Рейтинг с id 999 не найден");
    }
}

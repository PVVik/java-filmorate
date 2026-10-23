package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.*;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private FilmStorage filmStorage;

    @Mock
    private UserService userService;

    @Mock
    private MpaService mpaService;

    @Mock
    private GenreService genreService;

    @Mock
    private DirectorService directorService;

    @InjectMocks
    private FilmService filmService;

    @Test
    @DisplayName("Метод должен вернуть пустой список, если у пользователей нет общих фильмов")
    void getCommonFilms_shouldReturnEmptyList_whenNoCommonFilms() {
        given(filmStorage.getCommonFilms(1L, 2L)).willReturn(Collections.emptyList());
        given(genreService.getGenres()).willReturn(Collections.emptyList());
        given(mpaService.getMpas()).willReturn(Collections.emptyList());

        List<FilmDto> commonFilms = filmService.getCommonFilms(1L, 2L);

        assertThat(commonFilms).isNotNull();
        assertThat(commonFilms).isEmpty();
    }

    @Test
    @DisplayName("Метод должен вернуть общие фильмы в порядке убывания количества лайков")
    void getCommonFilms_shouldReturnFilmsSortedByPopularity() {
        Film mostPopular = Film.builder()
                .id(1L)
                .name("Most Popular")
                .likes(Set.of(1L, 2L, 3L))
                .build();

        Film lessPopular = Film.builder()
                .id(2L)
                .name("Less Popular")
                .likes(Set.of(4L))
                .build();

        given(filmStorage.getCommonFilms(1L, 2L)).willReturn(List.of(mostPopular, lessPopular));
        given(genreService.getGenres()).willReturn(Collections.emptyList());
        given(mpaService.getMpas()).willReturn(Collections.emptyList());

        List<FilmDto> commonFilms = filmService.getCommonFilms(1L, 2L);

        assertThat(commonFilms).hasSize(2);
        assertThat(commonFilms.get(0).getId()).isEqualTo(mostPopular.getId());
        assertThat(commonFilms.get(0).getName()).isEqualTo(mostPopular.getName());
        assertThat(commonFilms.get(1).getId()).isEqualTo(lessPopular.getId());
    }

    @Test
    @DisplayName("Метод должен выбросить NotFoundException, если пользователь не найден")
    void getCommonFilms_shouldThrow_whenUserNotFound() {
        doThrow(new NotFoundException("Пользователя с id 999 не существует"))
                .when(userService).checkUserExists(999L);

        assertThatThrownBy(() -> filmService.getCommonFilms(999L, 2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователя с id 999 не существует");
    }

    @Test
    @DisplayName("Сервис возвращает общие фильмы в том порядке, в котором их отсортировало хранилище")
    void getCommonFilms_shouldPreserveStorageOrder_whenLikesAreEqual() {
        Film olderFilm = Film.builder()
                .id(3L)
                .name("Old Film")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .likes(Set.of(1L, 2L))
                .build();

        Film newerFilm = Film.builder()
                .id(4L)
                .name("New Film")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .likes(Set.of(3L, 4L))
                .build();

        given(filmStorage.getCommonFilms(1L, 2L)).willReturn(List.of(newerFilm, olderFilm));
        given(genreService.getGenres()).willReturn(Collections.emptyList());
        given(mpaService.getMpas()).willReturn(Collections.emptyList());

        List<FilmDto> commonFilms = filmService.getCommonFilms(1L, 2L);

        assertThat(commonFilms).hasSize(2);
        assertThat(commonFilms.get(0).getName()).isEqualTo("New Film");
        assertThat(commonFilms.get(1).getName()).isEqualTo("Old Film");
    }

}

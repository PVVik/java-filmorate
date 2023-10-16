package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreDbService;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreControllerTest {
    private final GenreDbService genreService;
    private final Genre comedy = new Genre(1, "Комедия");
    private final Genre drama = new Genre(2, "Драма");
    private final Genre cartoon = new Genre(3, "Мультфильм");

    @Test
    public void getGenreById_shouldReturnGenreWithId1() {
        Assertions.assertEquals(comedy, genreService.getGenreById(1));
    }

    @Test
    public void getGenreById_shouldNotReturnGenreIfIdIsIncorrect() {
        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> genreService.getGenreById(134));
    }

    @Test
    public void getGenres_shouldReturnListOfGenres() {
        Assertions.assertTrue(genreService.getGenres().contains(drama));
        Assertions.assertTrue(genreService.getGenres().contains(comedy));
        Assertions.assertTrue(genreService.getGenres().contains(cartoon));
    }

}

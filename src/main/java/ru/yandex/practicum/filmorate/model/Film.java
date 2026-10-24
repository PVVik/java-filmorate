package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString
@Builder
public class Film implements Comparable<Film> {

    private long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;

    private Set<Long> likes;

    private Long mpaId;

    private List<Genre> genres;

    private List<Director> directors;

    @Override
    public int compareTo(Film o) {
        int thisLikesCount = (this.likes != null) ? this.likes.size() : 0;
        int otherLikesCount = (o.likes != null) ? o.likes.size() : 0;

        return Integer.compare(otherLikesCount, thisLikesCount);
    }
}

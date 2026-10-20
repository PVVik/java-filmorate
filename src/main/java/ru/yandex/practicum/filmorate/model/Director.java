package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString
@Builder
public class Director implements Comparable<Director> {

    private long id;

    private String name;

    @Override
    public int compareTo(Director o) {
        return Math.toIntExact(this.getId() - o.getId());
    }
}

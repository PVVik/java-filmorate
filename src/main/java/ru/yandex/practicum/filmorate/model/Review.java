package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(of = {"reviewId"})
@ToString
@Builder
public class Review implements Comparable<Review> {

    private long reviewId;
    private String content;
    private boolean isPositive;
    private long userId;
    private long filmId;
    private long useful;

    @Override
    public int compareTo(Review o) {
        return Math.toIntExact(o.useful - this.getUseful());
    }
}

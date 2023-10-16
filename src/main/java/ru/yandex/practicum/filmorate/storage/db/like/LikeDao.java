package ru.yandex.practicum.filmorate.storage.db.like;

public interface LikeDao {

    void like(Long filmId, Long userId);

    void dislike(Long filmId, Long userId);

    int countLikes(Long filmId);

    boolean isLiked(Long filmId, Long userId);
}

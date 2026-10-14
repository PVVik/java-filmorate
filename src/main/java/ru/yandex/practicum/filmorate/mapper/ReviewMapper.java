package ru.yandex.practicum.filmorate.mapper;

import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

public class ReviewMapper {

    public static Review mapToEntity(ReviewDto reviewDto) {
        return Review.builder().content(reviewDto.getContent()).isPositive(reviewDto.getIsPositive())
                .userId(reviewDto.getUserId()).filmId(reviewDto.getFilmId()).useful(0L).build();
    }

    public static ReviewDto mapToDto(Review review) {
        return new ReviewDto(review.getReviewId(), review.getContent(), review.isPositive(), review.getUserId(),
                review.getFilmId(), review.getUseful());
    }

    public static Review mapToUpdateEntity(Review review, ReviewDto reviewDto) {
        if(StringUtils.hasText(reviewDto.getContent())) {
            review.setContent(reviewDto.getContent());
        }
        if(reviewDto.getUserId() != null) {
            review.setUserId(reviewDto.getUserId());
        }
        if(reviewDto.getFilmId() != null) {
            review.setFilmId(reviewDto.getFilmId());
        }
        if(reviewDto.getIsPositive() != null) {
            review.setPositive(reviewDto.getIsPositive());
        }
        if(reviewDto.getUseful() != null) {
            review.setUseful(reviewDto.getUseful());
        }
        return review;
    }
}

DROP TABLE IF EXISTS mpa CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS genre CASCADE;
DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS friendship CASCADE;
DROP TABLE IF EXISTS likes CASCADE;

CREATE TABLE IF NOT EXISTS mpa (
    mpa_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mpa_name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    film_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_name VARCHAR NOT NULL,
    description VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    duration BIGINT NOT NULL,
    mpa_id BIGINT,
    CONSTRAINT chk_name_not_blank CHECK (TRIM(film_name) <> ''),
    CONSTRAINT chk_description_length CHECK (LENGTH(description) <= 200),
    CONSTRAINT chk_duration_positive CHECK (duration > 0),
    CONSTRAINT fk_film_mpa FOREIGN KEY (mpa_id) REFERENCES mpa(mpa_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS genre (
    genre_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    genre_name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_genre_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id BIGINT,
    genre_id BIGINT,
    CONSTRAINT fk_film_genre FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_genre_film FOREIGN KEY (genre_id) REFERENCES genre(genre_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_film_genre UNIQUE (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR NOT NULL,
    login VARCHAR NOT NULL,
    user_name VARCHAR NOT NULL,
    birthday DATE NOT NULL,
    CONSTRAINT chk_login_no_spaces CHECK (login NOT LIKE '% %'),
    CONSTRAINT chk_birthday_past CHECK (birthday < CURRENT_DATE)
);

CREATE TABLE IF NOT EXISTS friendship (
    friendship_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    is_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_no_self_friendship CHECK (user_id <> friend_id),
    CONSTRAINT fk_friendship_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_friend FOREIGN KEY (friend_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_friendship_unique UNIQUE (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS likes (
    likes_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id BIGINT,
    user_id BIGINT,
    CONSTRAINT fk_film_likes FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_user_likes FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_likes UNIQUE (film_id, user_id)
    );
# Filmorate

Filmorate — REST API приложения о фильмах. Пользователи могут оценивать фильмы, добавлять друзей, писать отзывы и получать рекомендации.

## Что реализовано командой

- создание, просмотр, обновление и удаление пользователей и фильмов;
- добавление друзей, просмотр списка и общих друзей;
- лайки фильмам и список популярных фильмов с фильтрацией по жанру и году;
- просмотр общих фильмов двух пользователей;
- поиск фильмов по названию и режиссёру;
- управление режиссёрами и получение их фильмов с сортировкой;
- создание, обновление и удаление отзывов, лайки и дизлайки отзывов;
- рекомендации фильмов на основе пересечения пользовательских лайков;
- лента событий пользователя;
- справочники жанров и рейтингов MPA.

## Технологии

- Java 21
- Spring Boot 3.5.12
- Spring JDBC (`JdbcTemplate`)
- H2
- Maven
- Lombok
- Bean Validation

## Запуск

Для запуска нужны JDK 21 и Maven. Из корневой папки проекта выполните:

```bash
mvn spring-boot:run
```

После запуска API доступно по адресу `http://localhost:8080`.

Запустить тесты:

```bash
mvn test
```

Собрать приложение:

```bash
mvn clean package
```

## База данных

Приложение использует файловую базу H2. Данные хранятся в `./db/filmorate`. Справочники жанров и рейтингов MPA загружаются из `data.sql`.

Консоль H2 доступна по адресу `http://localhost:8080/h2-console`.

Параметры подключения:

- JDBC URL: `jdbc:h2:file:./db/filmorate`
- User Name: `sa`
- Password: `password`

## ER-диаграмма

<img width="625" height="590" alt="ER-диаграмма Filmorate" src="https://github.com/user-attachments/assets/cb8cc48f-873c-4513-b92a-ddb5235114d5" />

## Примеры SQL-запросов

Получить фильм по идентификатору:

```sql
SELECT *
FROM films
WHERE film_id = 1;
```

Получить названия фильмов с рейтингом MPA `NC-17`:

```sql
SELECT f.film_name
FROM films AS f
JOIN mpa AS m ON m.mpa_id = f.mpa_id
WHERE m.mpa_name = 'NC-17';
```

## API

### Пользователи

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/users` | Создать пользователя |
| `PUT` | `/users` | Обновить пользователя |
| `GET` | `/users` | Получить всех пользователей |
| `GET` | `/users/{userId}` | Получить пользователя |
| `DELETE` | `/users/{userId}` | Удалить пользователя |
| `PUT` | `/users/{userId}/friends/{friendId}` | Добавить друга |
| `DELETE` | `/users/{userId}/friends/{friendId}` | Удалить друга |
| `GET` | `/users/{userId}/friends` | Получить список друзей |
| `GET` | `/users/{userId}/friends/common/{friendId}` | Получить общих друзей |
| `GET` | `/users/{userId}/recommendations` | Получить рекомендации |
| `GET` | `/users/{userId}/feed` | Получить ленту событий |

### Фильмы

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/films` | Добавить фильм |
| `PUT` | `/films` | Обновить фильм |
| `GET` | `/films` | Получить все фильмы |
| `GET` | `/films/{filmId}` | Получить фильм |
| `DELETE` | `/films/{filmId}` | Удалить фильм |
| `PUT` | `/films/{filmId}/like/{userId}` | Поставить лайк |
| `DELETE` | `/films/{filmId}/like/{userId}` | Удалить лайк |
| `GET` | `/films/popular` | Получить популярные фильмы |
| `GET` | `/films/common?userId={userId}&friendId={friendId}` | Получить общие фильмы |
| `GET` | `/films/search?query={текст}&by={поля}` | Найти фильмы |
| `GET` | `/films/director/{directorId}?sortBy={сортировка}` | Получить фильмы режиссёра |

У `/films/popular` параметры `genreId` и `year` необязательные. `count` ограничивает число результатов и по умолчанию равен `10`.

```text
GET /films/popular?count=10&genreId=1&year=2020
```

Для поиска параметр `by` принимает `title`, `director` или `title,director`. Параметр `sortBy` для фильмов режиссёра принимает `year` или `likes`.

### Отзывы

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/reviews` | Создать отзыв |
| `PUT` | `/reviews` | Обновить отзыв |
| `GET` | `/reviews/{id}` | Получить отзыв |
| `GET` | `/reviews?filmId={filmId}&limit={limit}` | Получить отзывы |
| `DELETE` | `/reviews/{id}` | Удалить отзыв |
| `PUT` | `/reviews/{reviewId}/like/{userId}` | Поставить лайк отзыву |
| `PUT` | `/reviews/{reviewId}/dislike/{userId}` | Поставить дизлайк отзыву |
| `DELETE` | `/reviews/{reviewId}/like/{userId}` | Удалить лайк с отзыва |
| `DELETE` | `/reviews/{reviewId}/dislike/{userId}` | Удалить дизлайк с отзыва |

Параметры `filmId` и `limit` у `GET /reviews` необязательные. Значение `limit` по умолчанию — `10`.

### Жанры и рейтинги MPA

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/genres` | Получить все жанры |
| `GET` | `/genres/{genreId}` | Получить жанр |
| `GET` | `/mpa` | Получить все рейтинги MPA |
| `GET` | `/mpa/{mpaId}` | Получить рейтинг MPA |

### Режиссёры

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/directors` | Добавить режиссёра |
| `PUT` | `/directors` | Обновить режиссёра |
| `GET` | `/directors` | Получить всех режиссёров |
| `GET` | `/directors/{id}` | Получить режиссёра |
| `DELETE` | `/directors/{id}` | Удалить режиссёра |
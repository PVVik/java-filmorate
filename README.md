# java-filmorate
Template repository for Filmorate project.

## ER-диаграмма:

<img width="625" height="590" alt="Снимок экрана 2026-09-21 162827" src="https://github.com/user-attachments/assets/cb8cc48f-873c-4513-b92a-ddb5235114d5" />

## Примеры запросов:

### Запрос на получение фильма по id:
    SELECT * 
    FROM film 
    WHERE film_id = 1;

### Запрос на получение названий всех фильмов с рейтингом NC-17:
    SELECT DISTINCT f.film_name 
    FROM film f 
    JOIN mpa m ON m.mpa_id = f.mpa_id 
    WHERE m.mpa_id = ( 
        SELECT mpa_id 
        FROM mpa 
        WHERE mpa_name = 'NC-17' 
    );

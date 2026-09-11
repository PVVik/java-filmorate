# java-filmorate
Template repository for Filmorate project.

## ER-диаграмма:

<img width="606" height="746" alt="Снимок экрана 2026-09-11 110603" src="https://github.com/user-attachments/assets/9b1a7aa3-ccce-4d15-bc7d-12956da23c48" />

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

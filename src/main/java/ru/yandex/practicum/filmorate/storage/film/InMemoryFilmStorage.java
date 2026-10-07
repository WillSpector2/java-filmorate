package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final List<Film> films = new ArrayList<>();

    @Override
    public Film add(Film film) {
        film.setId(getNextId());
        films.add(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Film existingFilm = getById(film.getId());
        films.set(films.indexOf(existingFilm), film);
        return film;
    }

    @Override
    public Film getById(int id) {
        return films.stream()
                .filter(film -> film.getId() == id)
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException(
                                "Film with id=" + id + " not found"
                        )
                );
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films);
    }

    @Override
    public boolean delete(int id) {
        Film film = getById(id);
        return films.remove(film);
    }

    private int getNextId() {
        return films.stream()
                .mapToInt(Film::getId)
                .max()
                .orElse(0) + 1;
    }
}

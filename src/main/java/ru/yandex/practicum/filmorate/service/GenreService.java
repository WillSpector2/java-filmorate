package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
public class GenreService {

    private final List<Genre> genres = List.of(
            createGenre(1, "Комедия"),
            createGenre(2, "Драма"),
            createGenre(3, "Мультфильм"),
            createGenre(4, "Триллер"),
            createGenre(5, "Документальный"),
            createGenre(6, "Боевик")
    );

    public List<Genre> getGenres() {
        return genres;
    }

    public Genre getGenreById(int id) {
        return genres.stream()
                .filter(genre -> genre.getId() == id)
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException("Жанр с id=" + id + " не найден")
                );
    }

    private static Genre createGenre(int id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genre;
    }
}

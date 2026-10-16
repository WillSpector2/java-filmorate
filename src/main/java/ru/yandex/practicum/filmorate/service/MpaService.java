package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Service
public class MpaService {

    private final List<Mpa> ratings = List.of(
            createMpa(1, "G"),
            createMpa(2, "PG"),
            createMpa(3, "PG-13"),
            createMpa(4, "R"),
            createMpa(5, "NC-17")
    );

    public List<Mpa> getRatings() {
        return ratings;
    }

    public Mpa getMpaById(int id) {
        return ratings.stream()
                .filter(rating -> rating.getId() == id)
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException("Рейтинг MPA с id=" + id + " не найден")
                );
    }

    private static Mpa createMpa(int id, String name) {
        Mpa mpa = new Mpa();
        mpa.setId(id);
        mpa.setName(name);
        return mpa;
    }
}
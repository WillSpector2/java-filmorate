CREATE TABLE IF NOT EXISTS users
(
    id
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    email
    VARCHAR
(
    254
) NOT NULL UNIQUE,
    login VARCHAR
(
    100
) NOT NULL UNIQUE,
    name VARCHAR
(
    100
),
    birthday DATE NOT NULL
    );

CREATE TABLE IF NOT EXISTS genres
(
    id
    INTEGER
    PRIMARY
    KEY,
    name
    VARCHAR
(
    50
) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS mpa_ratings
(
    id
    INTEGER
    PRIMARY
    KEY,
    name
    VARCHAR
(
    20
) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS films
(
    id
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL,
    description VARCHAR
(
    200
),
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL CHECK
(
    duration >
    0
),
    mpa_id INTEGER,
    CONSTRAINT fk_films_mpa
    FOREIGN KEY
(
    mpa_id
) REFERENCES mpa_ratings
(
    id
)
    );

CREATE TABLE IF NOT EXISTS film_genres
(
    film_id
    INTEGER
    NOT
    NULL,
    genre_id
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    film_id,
    genre_id
),
    CONSTRAINT fk_film_genres_film
    FOREIGN KEY
(
    film_id
) REFERENCES films
(
    id
) ON DELETE CASCADE,
    CONSTRAINT fk_film_genres_genre
    FOREIGN KEY
(
    genre_id
) REFERENCES genres
(
    id
)
    );

CREATE TABLE IF NOT EXISTS film_likes
(
    film_id
    INTEGER
    NOT
    NULL,
    user_id
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    film_id,
    user_id
),
    CONSTRAINT fk_film_likes_film
    FOREIGN KEY
(
    film_id
) REFERENCES films
(
    id
) ON DELETE CASCADE,
    CONSTRAINT fk_film_likes_user
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
)
  ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS friendships
(
    user_id
    INTEGER
    NOT
    NULL,
    friend_id
    INTEGER
    NOT
    NULL,
    status
    VARCHAR
(
    20
) NOT NULL DEFAULT 'UNCONFIRMED',
    PRIMARY KEY
(
    user_id,
    friend_id
),
    CONSTRAINT chk_friendship_not_self
    CHECK
(
    user_id
    <>
    friend_id
),
    CONSTRAINT chk_friendship_status
    CHECK
(
    status
    IN
(
    'UNCONFIRMED',
    'CONFIRMED'
)),
    CONSTRAINT fk_friendships_user
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_friend
    FOREIGN KEY
(
    friend_id
) REFERENCES users
(
    id
)
  ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_film_genres_genre_id
    ON film_genres(genre_id);

CREATE INDEX IF NOT EXISTS idx_film_likes_user_id
    ON film_likes(user_id);

CREATE INDEX IF NOT EXISTS idx_friendships_friend_id
    ON friendships(friend_id);

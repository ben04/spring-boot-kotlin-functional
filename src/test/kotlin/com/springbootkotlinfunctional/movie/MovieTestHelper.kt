package com.springbootkotlinfunctional.movie

import com.springbootkotlinfunctional.movie.document.Actor
import com.springbootkotlinfunctional.movie.document.Movie
import com.springbootkotlinfunctional.movie.document.Rating
import com.springbootkotlinfunctional.movie.document.Status
import java.time.LocalDate

fun createMovie(
        movieId: Long = 123,
        title: String = "Mission Impossible",
        actors: List<Actor> = listOf(
                createActor(),
                createActor("Jon", "Voight", LocalDate.of(1938, 12, 29))
        ),
        year: Int = 1996,
        rating: Rating = createRating(),
        status: String = Status.CREATED.status
) = Movie(movieId, title, actors, year, rating, status)

fun createActor(
        firstName: String = "Tom",
        lastName: String = "Cruise",
        dateOfBirth: LocalDate = LocalDate.of(1962, 7, 3)
) = Actor(firstName, lastName, dateOfBirth)

fun createRating(
        imdbRating: Float = 7.1f,
        rottenTomatoesRating: Int = 63,
        ageRating: String = "PG-13"
) = Rating(imdbRating, rottenTomatoesRating, ageRating)
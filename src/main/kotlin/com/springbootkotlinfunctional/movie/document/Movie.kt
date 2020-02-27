package com.springbootkotlinfunctional.movie.document

import org.springframework.data.annotation.Id
import java.time.LocalDate
import javax.annotation.Generated

data class Movie(
        @Id @Generated
        val movieId: Long,
        val title: String,
        val actors: List<Actor>,
        val year: Int,
        val rating: Rating,
        val status: String
)

data class Actor(
        val firstName: String,
        val lastName: String,
        val dateOfBirth: LocalDate
)

data class Rating(
        val imdbRating: Float,
        val rottenTomatoesRating: Int,
        val ageRating: String
)

enum class Status(val status: String) {
    CREATED("Created"),
    UPDATED("Updated");
}

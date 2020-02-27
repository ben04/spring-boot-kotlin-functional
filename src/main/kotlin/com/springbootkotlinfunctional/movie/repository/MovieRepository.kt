package com.springbootkotlinfunctional.movie.repository

import com.springbootkotlinfunctional.movie.document.Movie
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface MovieRepository: ReactiveMongoRepository<Movie, Long> {

    @Query("{'title': ?0}")
    fun findByTitle(title: String): Flux<Movie>

    @Query("{'rating.ageRating': ?0}")
    fun findByAgeRating(ageRating: String): Flux<Movie>
}

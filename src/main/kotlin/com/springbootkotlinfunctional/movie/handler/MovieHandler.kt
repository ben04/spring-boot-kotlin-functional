package com.springbootkotlinfunctional.movie.handler

import com.springbootkotlinfunctional.movie.document.Movie
import com.springbootkotlinfunctional.movie.document.Status
import com.springbootkotlinfunctional.movie.repository.MovieRepository
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

class MovieHandler(
        private val movieRepository: MovieRepository,
        private val webClientBuilder: WebClient.Builder,
        private val anotherApiUrl: String?) {

    @Suppress("UNUSED_PARAMETER")
    fun findAll(request: ServerRequest) =
            ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(movieRepository.findAll(Sort.by("title").ascending()).log())

    fun findById(request: ServerRequest) =
            movieRepository
                    .findById(request.pathVariable("movieId").toLong())
                    .flatMap {
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.just(it))
                    }
                    .switchIfEmpty(ServerResponse.notFound().build())

    fun findByAgeRating(request: ServerRequest) =
            ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(movieRepository.findByAgeRating(request.pathVariable("ageRating")).log())

    fun findByTitle(request: ServerRequest) =
            ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(movieRepository.findByTitle(request.pathVariable("title")).log())

    fun deleteById(request: ServerRequest) =
            ServerResponse.ok().body(movieRepository.deleteById(request.pathVariable("movieId").toLong()).log())

    fun update(request: ServerRequest) =
            request.bodyToMono(Movie::class.java)
                    .zipWith(movieRepository.findById(
                            request.pathVariable("movieId").toLong()).log()) { item, existingItem ->
                        item.copy(movieId = existingItem.movieId,
                                status = Status.UPDATED.status)
                    }
                    .log()
                    .flatMap(::saveAndRespond)
                    .switchIfEmpty(create(request))

    fun create(request: ServerRequest) = request
            .bodyToMono(Movie::class.java)
            .flatMap { movie ->
                ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(movieRepository.insert(movie).log().doOnSuccess {
                            postMovieToAnotherWebService(movie)
                        })
            }
            .switchIfEmpty(ServerResponse.notFound().build())

    private fun saveAndRespond(item: Movie) =
            ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(movieRepository.save(item).log())

    private fun postMovieToAnotherWebService(item: Movie) {
        webClientBuilder.build()
                .post()
                .uri("$anotherApiUrl/hello/world")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(item), Movie::class.java)
                .exchange()
                .log()
                .subscribe()
    }
}
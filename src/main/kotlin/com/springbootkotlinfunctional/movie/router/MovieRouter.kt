package com.springbootkotlinfunctional.movie.router

import com.springbootkotlinfunctional.movie.handler.MovieHandler
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

fun router(handler: MovieHandler) = router {
    accept(MediaType.APPLICATION_JSON_UTF8).nest {
        "/movies".nest {
            GET("/", handler::findAll)
            GET("/{movieId}", handler::findById)
            GET("/ageRating/{ageRating}", handler::findByAgeRating)
            GET("/title/{title}", handler::findByTitle)
            POST("/create", handler::create)
            PUT("/{movieId}", handler::update)
            DELETE("/{movieId}", handler::deleteById)
        }
    }
}

package com.springbootkotlinfunctional.movie

import com.springbootkotlinfunctional.movie.handler.MovieHandler
import com.springbootkotlinfunctional.movie.router.router
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class TutorialApplication

val beans = beans {
    val url = this.env.getProperty("anotherwebservice.api.url")

    bean { WebClient.builder() }
    bean { router(MovieHandler(ref(), ref(), url)) }
}

fun main(args: Array<String>) {
    runApplication<TutorialApplication>(*args) {
        addInitializers(beans)
    }
}

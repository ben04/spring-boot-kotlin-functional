package com.springbootkotlinfunctional.movie.handler

import com.springbootkotlinfunctional.movie.createMovie
import com.springbootkotlinfunctional.movie.document.Movie
import com.springbootkotlinfunctional.movie.document.Status
import com.springbootkotlinfunctional.movie.repository.MovieRepository
import com.springbootkotlinfunctional.movie.router.router
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MovieMockTest {

    @Autowired
    private lateinit var webClientBuilder: WebClient.Builder

    @Value("\${anotherwebservice.api.url}")
    private val anotherApiUrl: String? = null

    private val movieTest = createMovie()
    private val repositoryMock = mockk<MovieRepository>()

    private lateinit var handler: MovieHandler
    private lateinit var client: WebTestClient
    private val rnd = Random.nextLong()

    @BeforeAll
    fun init() {
        clearMocks(repositoryMock)
        handler = MovieHandler(repositoryMock, webClientBuilder, anotherApiUrl)
        client = WebTestClient.bindToRouterFunction(router(handler)).build()
    }

    @Nested
    inner class GetAnfrageData {
        @Test
        fun findById_valid() {
            every { repositoryMock.findById(rnd) } returns Mono.just(movieTest)
            val response = client.get()
                    .uri("/movies/$rnd")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Movie::class.java)
                    .returnResult()
                    .responseBody

            assertThat(response).isEqualTo(movieTest)
        }

        @Test
        fun findById_nonExisting() {
            every { repositoryMock.findById(rnd) } returns Mono.empty()
            client.get()
                    .uri("/movies/$rnd")
                    .exchange()
                    .expectStatus()
                    .isNotFound
        }

        @Test
        fun findAll() {
            every { repositoryMock.findAll(Sort.by("title").ascending()) } returns Flux.just(movieTest)
            val response = client.get()
                    .uri("/movies")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList(Movie::class.java)
                    .returnResult()
                    .responseBody

            assertThat(response?.size).isEqualTo(1)
            assertThat(response?.get(0)).isEqualTo(movieTest)
        }

        @Test
        fun findByAgeRating() {
            every { repositoryMock.findByAgeRating("PG-13") } returns Flux.just(movieTest)
            val response = client.get()
                    .uri("/movies/ageRating/PG-13")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList(Movie::class.java)
                    .returnResult()
                    .responseBody

            assertThat(response?.size).isEqualTo(1)
            assertThat(response?.get(0)).isEqualTo(movieTest)
        }

        @Test
        fun findByTitle() {
            every { repositoryMock.findByTitle("Mission Impossible") } returns Flux.just(movieTest)
            val response = client.get()
                    .uri("/movies/title/Mission Impossible")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList(Movie::class.java)
                    .returnResult()
                    .responseBody

            assertThat(response?.size).isEqualTo(1)
            assertThat(response?.get(0)).isEqualTo(movieTest)
        }
    }



    @Nested
    inner class NewAnfrageData {
        @Test
        fun create() {
            every { repositoryMock.insert(movieTest) } returns Mono.just(movieTest)

            val response = client.post()
                    .uri("/movies/create/")
                    .body(Mono.just(movieTest), Movie::class.java)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Movie::class.java)
                    .returnResult()
                    .responseBody

            assertThat(response).isEqualTo(movieTest)
        }
    }

    @Nested
    inner class UpdateAnfrageData {
        @Test
        fun update() {
            every { repositoryMock.findById(movieTest.movieId) } returns Mono.just(movieTest)

            val updatedMovie = movieTest.copy(status = Status.UPDATED.status)

            every { repositoryMock.save(any() as Movie) } returns Mono.just(updatedMovie)

            val response = client.put()
                    .uri("/movies/${movieTest.movieId}")
                    .body(Mono.just(movieTest), Movie::class.java)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Movie::class.java)
                    .returnResult()
                    .responseBody

            assertThat(response?.status).isEqualTo(Status.UPDATED.status)
            assertThat(response).isEqualTo(updatedMovie)
        }
    }

    @Nested
    inner class DeleteAnfrageData {
        @Test
        fun deleteById() {
            every { repositoryMock.deleteById(movieTest.movieId) } returns Mono.just(movieTest).then()
            val response = client.delete()
                    .uri("/movies/${movieTest.movieId}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Movie::class.java)
                    .returnResult()
                    .responseBody

            assertThat(response).isEqualTo(null)
        }
    }
}
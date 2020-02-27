package com.springbootkotlinfunctional.movie.handler

import com.springbootkotlinfunctional.movie.createMovie
import com.springbootkotlinfunctional.movie.createRating
import com.springbootkotlinfunctional.movie.document.Movie
import com.springbootkotlinfunctional.movie.document.Status
import com.springbootkotlinfunctional.movie.repository.MovieRepository
import com.springbootkotlinfunctional.movie.router.router
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.Alphanumeric::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MovieIntegrationTest {
    
    private val TEST_ID: Long = 999999999999999999
    private val DELETE_TEST_ID: Long = 999999999999999990

    @Value("\${anotherwebservice.api.url}")
    private val anotherApiUrl: String? = null

    @Autowired
    private lateinit var testClient: WebTestClient

    @Autowired
    private lateinit var webClientBuilder: WebClient.Builder

    @Autowired
    private lateinit var repository: MovieRepository

    private lateinit var handler: MovieHandler

    @BeforeAll
    internal fun before() {
        handler = MovieHandler(repository, webClientBuilder, anotherApiUrl)
        testClient = WebTestClient
                .bindToRouterFunction(router(handler))
                .configureClient()
                .baseUrl("/movies")
                .build()

        insertTestMovies()
    }

    private fun insertTestMovies() {
        testClient.post()
                .uri("/create/")
                .body(Mono.just(createMovie(TEST_ID)), Movie::class.java)
                .exchange()

        testClient.post()
                .uri("/create/")
                .body(Mono.just(createMovie(DELETE_TEST_ID)), Movie::class.java)
                .exchange()
    }

    @Nested
    inner class DeleteMovie {
        @Test
        fun delete_movieWithTestId() {
            testClient
                    .delete()
                    .uri("/$DELETE_TEST_ID")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody()
                    .isEmpty
        }
    }

    @Nested
    inner class GetAllMovies {
        @Test
        fun findAllMovies() {
            testClient
                    .get()
                    .uri("/")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBodyList(Movie::class.java)
        }
    }

    @Nested
    inner class GetMoviesByAgeRating {
        @Test
        fun findByAgeRating_PG13() {
            testClient
                    .get()
                    .uri("/ageRating/PG-13")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBodyList(Movie::class.java)
                    .hasSize(1)
        }

        @Test
        fun findByAgeRating_R() {
            testClient
                    .get()
                    .uri("/ageRating/R")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBodyList(Movie::class.java)
                    .hasSize(0)
        }
    }

    @Nested
    inner class GetMoviesByTitle {
        @Test
        fun findByTitle() {
            testClient
                    .get()
                    .uri("/title/Mission Impossible")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBodyList(Movie::class.java)
                    .hasSize(1)
        }
    }

    @Nested
    inner class GetMovieById {
        @Test
        fun findById() {
            testClient
                    .get()
                    .uri("/$TEST_ID")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.movieId").isNotEmpty
                    .jsonPath("$.title").isNotEmpty
                    .jsonPath("$.title").isEqualTo("Mission Impossible")
        }

        @Test
        fun findById_nonExisting_notFound() {
            testClient
                    .get()
                    .uri("/1345634656").exchange()
                    .expectStatus()
                    .isNotFound
        }
    }

    @Nested
    inner class InvalidUris {
        @Test
        fun findInvalidUri() {
            testClient
                    .get()
                    .uri("/create/1345634656")
                    .exchange()
                    .expectStatus()
                    .is4xxClientError
        }
    }

    @Nested
    inner class CreateAndUpdateMovie {
        @Test
        fun postNewMovieWithIdDeleteTestId() {
            testClient
                    .post()
                    .uri("/create/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Mono.just(createMovie(DELETE_TEST_ID)), Movie::class.java)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.movieId").isNotEmpty
                    .jsonPath("$.movieId").isEqualTo(DELETE_TEST_ID)
                    .jsonPath("$.title").isNotEmpty
                    .jsonPath("$.title").isEqualTo("Mission Impossible")
        }

        @Test
        fun updateMovieWithIdDeleteTestId() {
            val updatedMovie = createMovie(DELETE_TEST_ID, rating = createRating(imdbRating = 8.2f))

            testClient
                    .put()
                    .uri("/$DELETE_TEST_ID")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Mono.just(updatedMovie), Movie::class.java)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.movieId").isNotEmpty
                    .jsonPath("$.rating.imdbRating").isNotEmpty
                    .jsonPath("$.rating.imdbRating").isEqualTo(8.2f)
                    .jsonPath("$.status").isEqualTo(Status.UPDATED.status)
        }
    }

    @AfterAll
    internal fun tearDown() {
        testClient
                .delete()
                .uri("/$TEST_ID")
                .exchange()
                .expectStatus()
                .isOk

        testClient
                .delete()
                .uri("/$DELETE_TEST_ID")
                .exchange()
                .expectStatus()
                .isOk
    }
}
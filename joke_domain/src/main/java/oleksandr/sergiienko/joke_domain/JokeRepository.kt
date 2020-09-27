package oleksandr.sergiienko.joke_domain

interface JokeRepository {
  suspend fun getRandomJoke(): Result<Joke>
}
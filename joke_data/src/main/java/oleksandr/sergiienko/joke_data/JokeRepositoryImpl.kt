package oleksandr.sergiienko.joke_data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import oleksandr.sergiienko.joke_data.models.JokeResponse
import oleksandr.sergiienko.joke_domain.Joke
import oleksandr.sergiienko.joke_domain.JokeRepository
import oleksandr.sergiienko.joke_domain.Result

class JokeRepositoryImpl : JokeRepository {

  //FIXME move to base networking module
  @KtorExperimentalAPI
  private val okHttpKtor = HttpClient(CIO) {
    install(JsonFeature) {
      serializer = KotlinxSerializer(Json.nonstrict)
    }
  }

  override suspend fun getRandomJoke(): Result<Joke> {
    return withContext(Dispatchers.IO) {
      val urlString = "http://api.icndb.com/jokes/random"
      kotlin.runCatching {
        okHttpKtor.get<JokeResponse>(urlString)
      }.fold(
          onSuccess = { response -> Result.Success(Joke(response.value.joke)) },
          onFailure = {
            Result.Error(it)
          }
      )
    }
  }
}
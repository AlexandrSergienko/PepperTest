package oleksandr.sergiienko.joke_data.models

import kotlinx.serialization.Serializable

@Serializable
data class JokeResponse(val type: String, val value: JokeValue)

@Serializable
class JokeValue(val id: String, val joke: String)
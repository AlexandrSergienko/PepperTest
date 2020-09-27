package oleksandr.sergiienko.joke_domain

//FIXME move to base module
sealed class Result<out R> {
  data class Success<out T>(val data: T) : Result<T>()
  data class Error(val exception: Throwable) : Result<Nothing>()
}
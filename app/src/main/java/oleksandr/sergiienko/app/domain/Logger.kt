package oleksandr.sergiienko.app.domain

//FIXME Move to base module
interface Logger {
  fun i(message: String)
  fun d(message: String)
  fun e(message: String, error: Throwable)
}
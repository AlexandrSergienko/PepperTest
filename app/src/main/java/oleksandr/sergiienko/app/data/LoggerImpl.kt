package oleksandr.sergiienko.app.data

import android.util.Log
import oleksandr.sergiienko.app.domain.Logger


class LoggerImpl(
    private val tag: String
) : Logger {
  override fun i(message: String) {
    Log.i(tag, message)
  }

  override fun d(message: String) {
    Log.d(tag, message)
  }

  override fun e(message: String, error: Throwable) {
    Log.e(tag, message, error)
  }
}
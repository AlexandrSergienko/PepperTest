package oleksandr.sergiienko.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import oleksandr.sergiienko.app.data.LoggerImpl
import oleksandr.sergiienko.app.ui.MainViewModel
import oleksandr.sergiienko.joke_data.JokeRepositoryImpl

class VMFactory(application: PepperApplication) : ViewModelProvider.AndroidViewModelFactory(application) {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
      return MainViewModel(
          logger = LoggerImpl(modelClass.simpleName),
          jokeRepository = JokeRepositoryImpl()
      ) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
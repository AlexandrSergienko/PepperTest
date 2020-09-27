package oleksandr.sergiienko.app.ui

import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import oleksandr.sergiienko.app.R
import oleksandr.sergiienko.app.domain.Logger
import oleksandr.sergiienko.joke_domain.JokeRepository
import oleksandr.sergiienko.joke_domain.Result

class MainViewModel(
    private val logger: Logger,
    private val jokeRepository: JokeRepository
) : ViewModel(), RobotLifecycleCallbacks, EventViewModel<MainViewModel.Event> {
  @ExperimentalCoroutinesApi
  private val _events = ConflatedBroadcastChannel<Event>()

  @ExperimentalCoroutinesApi
  @FlowPreview
  override val events: Flow<Event> = _events.asFlow()

  private val chatViewModel = ChatViewModel()
  private val animationViewModel = AnimationViewModel(arrayOf(R.raw.bow))

  // TODO provide using DI
  private val delegateSet = setOf(chatViewModel, animationViewModel)

  init {
    delegateSet.forEach {
      viewModelScope.launch { // context of the parent, main runBlocking coroutine
        it.events.collect { value -> handleInternalEvent(value) }
      }
    }
  }

  private fun handleInternalEvent(value: InternalEvent) {
    when (value) {
      is InternalEvent.OnNewMessage -> _events.offer(Event.OnNewMessage(value.message, value.isRobot))
      is InternalEvent.OnAnimationEnded -> handleAnimationEnded(value.animationId)
      is InternalEvent.OnAnimationStarted -> handleAnimationStarted(value.animationId)
      is InternalEvent.OnChatStarted -> chatViewModel.runStartBookmark()
    }
  }

  fun startBowAnimation() {
    viewModelScope.launch {
      animationViewModel.startAnimation(R.raw.bow)
    }
  }

  fun makeAJoke() {
    viewModelScope.launch {
      val result = jokeRepository.getRandomJoke()
      logger.d("makeAJoke result = $result")
      if (result is Result.Success) {
        _events.offer(Event.OnNewJoke(result.data.joke))
      } else {
        _events.offer(Event.OnErrorOccurred)
      }
    }
  }

  private fun handleAnimationStarted(@RawRes animationId: Int) {
    if (animationId == R.raw.bow) {
      chatViewModel.runEndBookmark()
    }
  }

  private fun handleAnimationEnded(@RawRes animationId: Int) {
    if (animationId == R.raw.bow) {
      logger.d("Bow animation ended")
    }
  }

  override fun onRobotFocusGained(qiContext: QiContext) {
    // Bind the conversational events to the view.
    logger.i("onRobotFocusGained qiContext=$qiContext")
    delegateSet.forEach {
      it.onRobotFocusGained(qiContext)
    }
  }

  override fun onRobotFocusLost() {
    logger.i("onRobotFocusLost ")
    // Remove the listeners from the Chat action.
    delegateSet.forEach {
      it.onRobotFocusLost()
    }
  }

  override fun onRobotFocusRefused(reason: String) {
    logger.i("onRobotFocusRefused reason=$reason")
    delegateSet.forEach {
      it.onRobotFocusRefused(reason)
    }
  }

  sealed class Event {
    data class OnNewMessage(val message: String, val isRobot: Boolean) : Event()
    data class OnNewJoke(val joke: String) : Event()
    object OnErrorOccurred : Event()
  }

  sealed class InternalEvent {
    object OnChatStarted : InternalEvent()
    object OnChatEnded : InternalEvent()
    data class OnNewMessage(val message: String, val isRobot: Boolean) : InternalEvent()
    data class OnAnimationStarted(@RawRes val animationId: Int) : InternalEvent()
    data class OnAnimationEnded(@RawRes val animationId: Int, val success: Boolean) : InternalEvent()
  }

}

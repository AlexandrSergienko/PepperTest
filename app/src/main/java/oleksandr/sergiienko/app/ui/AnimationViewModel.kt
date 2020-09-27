package oleksandr.sergiienko.app.ui

import android.util.SparseArray
import androidx.annotation.RawRes
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext
import oleksandr.sergiienko.app.R
import oleksandr.sergiienko.app.data.LoggerImpl

internal class AnimationViewModel(
    private val animations: Array<Int>
) : ViewModelPepperDelegate<MainViewModel.InternalEvent> {

  //FIXME move to DI system, with LoggerFactory
  private val logger = LoggerImpl("AnimationViewModel");

  @ExperimentalCoroutinesApi
  private val _events = ConflatedBroadcastChannel<MainViewModel.InternalEvent>()

  override val events: Flow<MainViewModel.InternalEvent> = _events.asFlow()

  private val preparedAnimate = SparseArray<Animate>()


  suspend fun startAnimation(@RawRes id: Int) {
    withContext(Dispatchers.IO) {
      logger.i("startBowAnimation id=$id")
      val animate = preparedAnimate[id]
      // Add an on started listener to the animate action.

      animate.addOnStartedListener {
        _events.offer(MainViewModel.InternalEvent.OnAnimationStarted(id))
      }
      // Run the animate action asynchronously.
      val animateFuture = animate.async().run()
      // Add a lambda to the action execution.
      animateFuture.thenConsume {
        _events.offer(MainViewModel.InternalEvent.OnAnimationEnded(id, it.isSuccess))
      }
    }
  }


  private fun prepareAnimation(@RawRes resId: Int, qiContext: QiContext) {
    val animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
        .withResources(R.raw.bow) // Set the animation resource.
        .build() // Build the animation.
    // Create an animate action.
    val animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
        .withAnimation(animation) // Set the animation.
        .build() // Build the animate action.
    preparedAnimate.append(resId, animate)
  }

  override fun onRobotFocusGained(qiContext: QiContext?) {
    qiContext?.let { context ->
      animations.forEach { id ->
        prepareAnimation(id, context)
      }
    }
  }

  override fun onRobotFocusLost() {
    preparedAnimate.clear()
  }

  override fun onRobotFocusRefused(reason: String?) {
    preparedAnimate.clear()
  }
}
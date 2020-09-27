package oleksandr.sergiienko.app.ui

import android.os.Bundle
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import oleksandr.sergiienko.app.PepperApplication
import oleksandr.sergiienko.app.R
import oleksandr.sergiienko.app.VMFactory

class MainActivity : RobotActivity() {

  // FIXME unable to provide viewModel through correct provider because of qiContext retaining.
  //  private val viewModel by viewModels<MainViewModel>{VMFactory(application as PepperApplication)}
  private val viewModel by lazy { VMFactory(application as PepperApplication).create(MainViewModel::class.java) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)
    // Register the RobotLifecycleCallbacks to this Activity.
    button_animation.setOnClickListener {
      viewModel.startBowAnimation()
    }
    button_joke.setOnClickListener {
      viewModel.makeAJoke()
    }
    MainScope().launch {
      viewModel.events.collect { event ->
        handleEvents(event)
      }
    }


    QiSDK.register(this, viewModel)
  }

  private fun handleEvents(event: MainViewModel.Event) {
    when (event) {
      is MainViewModel.Event.OnNewJoke -> edit_text_joke.setText(event.joke)
      is MainViewModel.Event.OnNewMessage -> chat_workaround.text = "${chat_workaround.text} \n" +
          if (event.isRobot) {
            "Robot: "
          } else {
            "You: "
          } +
          "${event.message}"
      //FIXME make regular Recycler View
      is MainViewModel.Event.OnErrorOccurred -> chat_workaround.text = "Some error occured, see in logs"
    }
  }

  override fun onDestroy() {
    // Unregister the RobotLifecycleCallbacks for this Activity.
    QiSDK.unregister(this, viewModel)
    super.onDestroy()
  }
}

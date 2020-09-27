package oleksandr.sergiienko.app.ui

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.AutonomousReactionImportance
import com.aldebaran.qi.sdk.`object`.conversation.AutonomousReactionValidity
import com.aldebaran.qi.sdk.`object`.conversation.Bookmark
import com.aldebaran.qi.sdk.`object`.conversation.Chat
import com.aldebaran.qi.sdk.`object`.conversation.QiChatbot
import com.aldebaran.qi.sdk.builder.ChatBuilder
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder
import com.aldebaran.qi.sdk.builder.TopicBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import oleksandr.sergiienko.app.R
import oleksandr.sergiienko.app.data.LoggerImpl

internal class ChatViewModel() : ViewModelPepperDelegate<MainViewModel.InternalEvent> {

  //FIXME move to DI system, with LoggerFactory
  private val logger = LoggerImpl("ChatViewModel")
  private var _chat: Chat? = null

  @ExperimentalCoroutinesApi
  private val _events = ConflatedBroadcastChannel<MainViewModel.InternalEvent>()

  override val events: Flow<MainViewModel.InternalEvent> = _events.asFlow()

  private lateinit var _startBookmark: Bookmark
  private lateinit var _endBookmark: Bookmark
  private lateinit var _qiChatbot: QiChatbot

  @ExperimentalCoroutinesApi
  private fun startChat(qiContext: QiContext?) {
    logger.i("startChat qiContext=$qiContext")
    qiContext?.let {

      // Create a topic.
      val topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
          .withResource(R.raw.welcoming) // Set the topic resource.
          .build() // Build the topic.

      topic.bookmarks["welcoming"]?.let {
        _startBookmark = it
      }
      topic.bookmarks["goodbye"]?.let {
        _endBookmark = it
      }

      // Create a new QiChatbot.
      _qiChatbot = QiChatbotBuilder.with(qiContext)
          .withTopic(topic)
          .build()

      // Create a new Chat action.
      val chat = ChatBuilder.with(qiContext)
          .withChatbot(_qiChatbot)
          .build()
          .also { this._chat = it }

      // Add an on started listener to the Chat action.
      chat.addOnStartedListener {
        _events.offer(MainViewModel.InternalEvent.OnChatStarted)
      }
      chat.addOnHeardListener {
        if (it.text.isNotBlank()) {
          val heardMessage = MainViewModel.InternalEvent.OnNewMessage(it.text, false)
          logger.d("OnHeardListener message=$heardMessage")
          _events.offer(heardMessage)
        }
      }
      chat.addOnSayingChangedListener {
        val sayingMessage = MainViewModel.InternalEvent.OnNewMessage(it.text, true)
        logger.d("OnHeardListener message=$sayingMessage")
        _events.offer(sayingMessage)
      }

      // Run the Chat action asynchronously.
      val chatFuture = chat.async().run()

      // Add a lambda to the action execution.
      chatFuture.thenConsume {
        _events.offer(MainViewModel.InternalEvent.OnChatEnded)
        if (it.hasError()) {
          val message = "Discussion finished with error."
          logger.e(message, it.error)
        }
      }
    }
  }

  internal fun runStartBookmark() {
    _qiChatbot.async().goToBookmark(_startBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
  }

  internal fun runEndBookmark() {
    _qiChatbot.async().goToBookmark(_endBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
  }

  override fun onRobotFocusGained(qiContext: QiContext?) {
    startChat(qiContext)
  }

  override fun onRobotFocusLost() {
    _chat?.removeAllOnHeardListeners()
    _chat?.removeAllOnStartedListeners()
    _chat?.removeAllOnSayingChangedListeners()
    _chat = null
  }

  override fun onRobotFocusRefused(reason: String?) {
    _chat?.removeAllOnHeardListeners()
    _chat?.removeAllOnStartedListeners()
    _chat?.removeAllOnSayingChangedListeners()
    _chat = null
  }
}
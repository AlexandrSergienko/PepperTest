package oleksandr.sergiienko.app.ui

import kotlinx.coroutines.flow.Flow

interface EventViewModel<T> {
  val events: Flow<T>
}
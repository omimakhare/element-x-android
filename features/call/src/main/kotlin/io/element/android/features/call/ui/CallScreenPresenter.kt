/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.call.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.appnav.di.MatrixClientsHolder
import io.element.android.features.call.CallType
import io.element.android.features.call.utils.WidgetMessageInterceptor
import io.element.android.features.call.widgetdriver.CallWidgetDriver
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.UUID

class CallScreenPresenter @AssistedInject constructor(
    private val matrixClientsHolder: MatrixClientsHolder,
    @Assisted private val inputs: CallType,
    @Assisted private val navigator: CallScreenNavigator,
) : Presenter<CallScreenState> {

    @AssistedFactory
    interface Factory {
        fun create(inputs: CallType, navigator: CallScreenNavigator): CallScreenPresenter
    }

    // TODO: Move to a config file
    companion object {
        private const val BASE_URL = "https://call.element.io"
    }

    private val isInWidgetMode = inputs is CallType.RoomCall

    @Composable
    override fun present(): CallScreenState {
        val urlState = remember { mutableStateOf<Async<String>>(Async.Uninitialized) }
        val callWidgetDriver = remember { mutableStateOf<CallWidgetDriver?>(null) }
        val messageInterceptor = remember { mutableStateOf<WidgetMessageInterceptor?>(null) }

        LaunchedEffect(Unit) {
            loadUrl(inputs, urlState, callWidgetDriver)
        }

        LaunchedEffect(callWidgetDriver.value != null) {
            callWidgetDriver.value?.let { driver ->
                driver.incomingMessages
                    .onEach {
                        // Relay message to the WebView
                        messageInterceptor.value?.sendMessage(it)
                    }
                    .launchIn(this)

                driver.start()
            }
        }

        LaunchedEffect(messageInterceptor.value != null) {
            messageInterceptor.value?.interceptedMessages
                ?.onEach {
                    // Relay message to Widget Driver
                    callWidgetDriver.value?.send(it)

                    val parsedMessage = parseMessage(it)
                    if (parsedMessage?.direction == WidgetMessage.Direction.FromWidget && parsedMessage.action == WidgetMessage.Action.HangUp) {
                        navigator.close()
                    }
                }
                ?.launchIn(this)
        }

        fun handleEvents(event: CallScreeEvents) {
            when (event) {
                is CallScreeEvents.Hangup -> {
                    callWidgetDriver.value?.close()
                    navigator.close()
                }
                is CallScreeEvents.SetupMessageChannels -> {
                    messageInterceptor.value = WidgetMessageInterceptor(event.webView)
                }
            }
        }

        return CallScreenState(
            urlState = urlState.value,
            isInWidgetMode = isInWidgetMode,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.loadUrl(
        inputs: CallType,
        urlState: MutableState<Async<String>>,
        callWidgetDriver: MutableState<CallWidgetDriver?>,
    ) = launch {
        urlState.runCatchingUpdatingState {
            when (inputs) {
                is CallType.ExternalUrl -> {
                    inputs.url
                }
                is CallType.RoomCall -> {
                    val room = matrixClientsHolder.getOrNull(inputs.sessionId)
                        ?.getRoom(inputs.roomId)
                        ?: error("Room not found")
                    val driver = CallWidgetDriver.create(room, BASE_URL, UUID.randomUUID().toString())
                    callWidgetDriver.value = driver
                    driver.url
                }
            }
        }
    }

    private fun parseMessage(message: String): WidgetMessage? {
        val jsonDecoder = Json { ignoreUnknownKeys = true }
        return runCatching { jsonDecoder.decodeFromString(WidgetMessage.serializer(), message) }
            .onFailure { Timber.e(it) }
            .getOrNull()
    }

}

@Serializable
data class WidgetMessage(
    @SerialName("api") val direction: Direction,
    @SerialName("action") val action: Action,
) {

    @Serializable
    enum class Direction {
        @SerialName("fromWidget")
        FromWidget,
        @SerialName("toWidget")
        ToWidget
    }

    @Serializable
    enum class Action {
        @SerialName("im.vector.hangup")
        HangUp
    }
}

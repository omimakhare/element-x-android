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

package io.element.android.features.call.widgetdriver

import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.impl.widget.DefaultCallWidgetSettingsProvider

class CallWidgetDriver private constructor(
    val url: String,
    val id: String,
    private val widgetDriver: MatrixWidgetDriver,
) : AutoCloseable {

    companion object {
        suspend fun create(
            room: MatrixRoom,
            baseUrl: String,
            clientId: String,
        ): CallWidgetDriver {
            val widgetSettings = DefaultCallWidgetSettingsProvider().provide(baseUrl)
            val callUrl = room.generateWidgetWebViewUrl(widgetSettings, clientId).getOrThrow()
            val widgetDriver = room.getWidgetDriver(widgetSettings).getOrThrow()
            return CallWidgetDriver(callUrl, widgetSettings.id, widgetDriver)
        }
    }

    val incomingMessages = widgetDriver.incomingMessages

    suspend fun start() {
        widgetDriver.run()
    }

    suspend fun send(message: String) {
        widgetDriver.send(message)
    }

    override fun close() {
        widgetDriver.close()
    }
}

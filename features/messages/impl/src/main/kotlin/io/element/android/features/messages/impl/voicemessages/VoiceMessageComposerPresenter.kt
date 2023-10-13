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

package io.element.android.features.messages.impl.voicemessages

import android.Manifest
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.textcomposer.model.PressEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.voicerecorder.api.VoiceRecorder
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@SingleIn(RoomScope::class)
class VoiceMessageComposerPresenter @Inject constructor(
    private val appCoroutineScope: CoroutineScope,
    private val voiceRecorder: VoiceRecorder,
    private val analyticsService: AnalyticsService,
    private val room: MatrixRoom,
    private val mediaSender: MediaSender,
    permissionsPresenterFactory: PermissionsPresenter.Factory
) : Presenter<VoiceMessageComposerState> {
    private val permissionsPresenter = permissionsPresenterFactory.create(Manifest.permission.RECORD_AUDIO)
    @Composable
    override fun present(): VoiceMessageComposerState {
        val localCoroutineScope = rememberCoroutineScope()
        var isRecording by remember { mutableStateOf(false) }
        val voiceLevel by voiceRecorder.level.collectAsState(initial = 0.0)

        val permissionState = permissionsPresenter.present()

        fun onRecordButtonPress(event: VoiceMessageComposerEvents.RecordButtonEvent) = when(event.pressEvent) {
            PressEvent.PressStart ->  {
                Timber.i("Voice message record button pressed")
                when {
                    permissionState.permissionGranted -> {
                        try {
                            localCoroutineScope.launch {
                                Timber.i("Voice message started recording")
                                voiceRecorder.startRecord(groupId = room.roomId.value)
                                // TODO: test error throw
                            }
                        } catch(e: SecurityException) {
                            Timber.e("Voice message error", e)
                            analyticsService.trackError(VoiceMessageException.FileMissing("Expected permission to record but none", e))
                        }
                        isRecording = true
                    }
                    permissionState.shouldShowRationale -> {
                        Timber.i("Voice message permission rationale needed")
                        // show the rationale
                    }
                    else -> {
                        permissionState.eventSink(PermissionsEvents.RequestPermissions)
                    }
                }
            }
            PressEvent.LongPressEnd -> {
                Timber.i("Voice message finished recording")

                // TODO refactor out
                appCoroutineScope.launch {
                    val file = voiceRecorder.stopRecord()
                    if (file == null) {
                        Timber.e("Voice message error: file was null")
                        analyticsService.trackError(VoiceMessageException.FileMissing("File was null after recording"))
                        return@launch
                    }
                    val result = mediaSender.sendVoiceMessage(
                        uri = file.toUri(),
                        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension) ?: "audio/ogg",
                        progressCallback = null,
                    )

                    if(result.isFailure) {
                        Timber.e("Voice message error: ${result.exceptionOrNull()}")
                    }
                }
                isRecording = false
            }
            PressEvent.Tapped -> {
                Timber.i("Voice message deleted")
                localCoroutineScope.launch {
                    voiceRecorder.stopRecord()
                    voiceRecorder.deleteRecording()
                }
                isRecording = false
            }
        }


        fun handleEvents(event: VoiceMessageComposerEvents) {
            when (event) {
                is VoiceMessageComposerEvents.RecordButtonEvent -> onRecordButtonPress(event)
            }
        }

        return VoiceMessageComposerState(
            voiceMessageState = when (isRecording) {
                true -> VoiceMessageState.Recording(level = voiceLevel)
                false -> VoiceMessageState.Idle
            },
            eventSink = { handleEvents(it) },
        )
    }
}

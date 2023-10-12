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

package io.element.android.libraries.voicerecorder.impl

import android.Manifest
import androidx.annotation.RequiresPermission
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.voicerecorder.api.VoiceRecorder
import io.element.android.libraries.voicerecorder.impl.audio.Audio
import io.element.android.libraries.voicerecorder.impl.audio.AudioConfig
import io.element.android.libraries.voicerecorder.impl.audio.AudioRecorder
import io.element.android.libraries.voicerecorder.impl.audio.Encoder
import io.element.android.libraries.voicerecorder.impl.audio.calculateAudioLevel
import io.element.android.libraries.voicerecorder.impl.file.VoiceFileManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@SingleIn(RoomScope::class) // TODO: Remove?
@ContributesBinding(RoomScope::class)
class VoiceRecorderImpl @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val encoder: Encoder,
    private val fileManager: VoiceFileManager,
    private val config: AudioConfig,
) : VoiceRecorder {
    private var outputFile: File? = null

    private var audioRecorder: AudioRecorder? = null

    override val level = MutableStateFlow(0.0)
    private var recordingJob: Job? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override suspend fun startRecord(groupId: String) {
        Timber.i("Voice recorder started recording")
        outputFile = fileManager.createFile(groupId).also {
            encoder.init(it.absolutePath)
        }

        val audioRecorder = AudioRecorder(config, dispatchers).also { audioRecorder = it }

        recordingJob = audioRecorder.record { audio ->
            when(audio) {
                is Audio.Data -> {
                    level.emit(calculateAudioLevel(audio.buffer))
                    encoder.encode(audio.buffer, audio.readSize)
                }
                is Audio.Error -> {
                    Timber.e("Voice message error: code=${audio.audioRecordErrorCode}")
                    level.emit(0.0)
                }
            }
        }
    }

    /**
     * Stop the current recording.
     *
     * Call [deleteRecording] to delete any recorded audio.
     */
    override suspend fun stopRecord() {
        Timber.i("Voice recorder stopped recording")
        recordingJob?.cancel()

        audioRecorder?.stop()
        audioRecorder = null
        encoder.release()
        level.emit(0.0)
    }

    /**
     * Stop the current recording and delete the output file.
     */
    override fun deleteRecording() {
        outputFile?.delete()
        outputFile = null
    }

}

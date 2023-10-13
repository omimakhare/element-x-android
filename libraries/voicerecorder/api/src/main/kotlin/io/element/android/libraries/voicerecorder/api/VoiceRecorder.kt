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

package io.element.android.libraries.voicerecorder.api

import android.Manifest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Audio recorder which records audio to opus/ogg files.
 */
interface VoiceRecorder {
    /**
     * Start a recording.
     *
     * Call [stopRecord] to stop the recording and release resources.
     *
     * @param groupId A unique ID to identify a group of recordings. Used to generate the cache subdirectory.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    suspend fun startRecord(groupId: String)

    /**
     * Stop the current recording.
     *
     * Call [deleteRecording] to delete any recorded audio.
     */
    suspend fun stopRecord(): File?

    /**
     * Stop the current recording and delete the output file.
     */
    fun deleteRecording()

    /**
     * Get the current audio level of the recording as a fraction of 1.
     */
    val level: StateFlow<Double>
}

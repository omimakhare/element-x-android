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

package io.element.android.libraries.voicerecorder.impl.audio

import io.element.android.opusencoder.OggOpusEncoder
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

/**
 * Safe wrapper for OggOpusEncoder.
 */
class Encoder @Inject constructor(
    private val encoderProvider: Provider<OggOpusEncoder>,
    config: AudioConfig,
) {
    private val bitRate = config.bitRate
    private val sampleRate = config.format.sampleRateHz().toLibModel()

    private var encoder: OggOpusEncoder? = null
    fun init(
        filePath: String,
    ) {
        encoder?.release()
        encoder = encoderProvider.get().apply {
            init(filePath, sampleRate)
            setBitrate(bitRate)
            // TODO check encoder application: 2048 (voice, default is typically 2049 as audio)
        }
    }

    fun encode(
        buffer: ShortArray,
        readSize: Int,
    ) {
        encoder?.encode(buffer, readSize)
            ?: Timber.w("Can't encode when encoder not initialized")
    }

    fun release() {
        encoder?.release()
            ?: Timber.w("Can't release encoder that is not initialized")
        encoder = null
    }
}

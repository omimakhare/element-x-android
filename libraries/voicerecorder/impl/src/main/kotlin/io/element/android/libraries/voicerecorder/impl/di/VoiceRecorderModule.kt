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

package io.element.android.libraries.voicerecorder.impl.di

import android.media.AudioFormat
import android.media.MediaRecorder
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.voicerecorder.impl.audio.AudioConfig
import io.element.android.libraries.voicerecorder.impl.audio.toHz
import io.element.android.libraries.voicerecorder.impl.file.VoiceFileConfig
import io.element.android.opusencoder.OggOpusEncoder
import io.element.android.opusencoder.configuration.SampleRate

@Module
@ContributesTo(RoomScope::class)
object VoiceRecorderModule {
    @Provides
    fun provideAudioConfig(): AudioConfig =
        AudioConfig(
            format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SampleRate.Rate48kHz.toHz().value)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build(),
            bitRate = 24 * 1024, // TODO shouldn't this be 24 * 1000?
            source = MediaRecorder.AudioSource.MIC,
        )

    @Provides
    fun provideVoiceFileConfig(): VoiceFileConfig =
        VoiceFileConfig(
            cacheSubdir = "voice_recordings",
            fileExt = "ogg"
        )

    @Provides
    fun provideOggOpusEncoder(): OggOpusEncoder = OggOpusEncoder.create()
}

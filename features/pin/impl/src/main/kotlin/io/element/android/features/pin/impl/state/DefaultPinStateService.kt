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

package io.element.android.features.pin.impl.state

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.pin.api.PinState
import io.element.android.features.pin.api.PinStateService
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val GRACE_PERIOD_IN_MILLIS = 90 * 1000L

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultPinStateService @Inject constructor(
    private val featureFlagService: FeatureFlagService,
) : PinStateService {

    private val _pinState = MutableStateFlow<PinState>(PinState.Unlocked)
    override val pinState: StateFlow<PinState> = _pinState

    private var lockJob: Job? = null

    override suspend fun unlock() {
        if (featureFlagService.isFeatureEnabled(FeatureFlags.PinUnlock)) {
            _pinState.value = PinState.Unlocked
        }
    }

    override suspend fun entersForeground() {
        lockJob?.cancel()
    }

    override suspend fun entersBackground() = coroutineScope {
        lockJob = launch {
            if (featureFlagService.isFeatureEnabled(FeatureFlags.PinUnlock)) {
                delay(GRACE_PERIOD_IN_MILLIS)
                _pinState.value = PinState.Locked
            }
        }
    }
}

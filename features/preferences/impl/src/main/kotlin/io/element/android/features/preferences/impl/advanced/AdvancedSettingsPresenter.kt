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

package io.element.android.features.preferences.impl.advanced

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.features.preferences.api.store.PreferencesStore
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

class AdvancedSettingsPresenter @Inject constructor(
    private val preferencesStore: PreferencesStore,
    private val featureFlagService: FeatureFlagService,
) : Presenter<AdvancedSettingsState> {

    @Composable
    override fun present(): AdvancedSettingsState {
        val localCoroutineScope = rememberCoroutineScope()
        val isRichTextEditorEnabled by preferencesStore
            .isRichTextEditorEnabledFlow()
            .collectAsState(initial = false)
        val isDeveloperModeEnabled by preferencesStore
            .isDeveloperModeEnabledFlow()
            .collectAsState(initial = false)
        val customElementCallBaseUrl by preferencesStore
            .getCustomElementCallBaseUrlFlow()
            .collectAsState(initial = null)

        var canDisplayElementCallSettings by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            canDisplayElementCallSettings = featureFlagService.isFeatureEnabled(FeatureFlags.InRoomCalls)
        }

        fun handleEvents(event: AdvancedSettingsEvents) {
            when (event) {
                is AdvancedSettingsEvents.SetRichTextEditorEnabled -> localCoroutineScope.launch {
                    preferencesStore.setRichTextEditorEnabled(event.enabled)
                }
                is AdvancedSettingsEvents.SetDeveloperModeEnabled -> localCoroutineScope.launch {
                    preferencesStore.setDeveloperModeEnabled(event.enabled)
                }
                is AdvancedSettingsEvents.SetCustomElementCallBaseUrl -> localCoroutineScope.launch {
                    preferencesStore.setCustomElementCallBaseUrl(event.baseUrl)
                }
            }
        }

        return AdvancedSettingsState(
            isRichTextEditorEnabled = isRichTextEditorEnabled,
            isDeveloperModeEnabled = isDeveloperModeEnabled,
            customElementCallBaseUrlState = if (canDisplayElementCallSettings) {
                CustomElementCallBaseUrlState(
                    baseUrl = customElementCallBaseUrl,
                    validator = ::customElementCallUrlValidator,
                )
            } else null,
            eventSink = ::handleEvents
        )
    }

    private fun customElementCallUrlValidator(url: String?): Boolean {
        return runCatching {
            val parsedUrl = URL(url)
            if (parsedUrl.protocol.isNullOrBlank()) error("Missing protocol")
            if (parsedUrl.host.isNullOrBlank()) error("Missing host")
        }.isSuccess
    }
}

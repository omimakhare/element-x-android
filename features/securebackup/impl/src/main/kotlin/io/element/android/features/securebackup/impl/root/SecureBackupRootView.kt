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

package io.element.android.features.securebackup.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SecureBackupRootView(
    state: SecureBackupRootState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onSetupClicked: () -> Unit,
    onChangeClicked: () -> Unit,
    onDisableClicked: () -> Unit,
    onLearnMoreClicked: () -> Unit,
) {
    // TODO i18n
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_secure_backup),
    ) {
        when (state.secureBackupState) {
            SecureBackupState.DISABLED,
            SecureBackupState.NOT_SETUP -> {
                PreferenceText(
                    title = "Set up recovery",
                    subtitle = "You will need recovery to get access to your encrypted messages if you lose all your devices or are signed out of Element everywhere.",
                    onClick = onSetupClicked,
                )
            }
            SecureBackupState.SETUP -> {
                PreferenceText(
                    title = "Change recovery key",
                    onClick = onChangeClicked,
                )
            }
        }
        when (state.secureBackupState) {
            SecureBackupState.NOT_SETUP,
            SecureBackupState.SETUP ->
                PreferenceText(
                    title = "Disable backup",
                    subtitle = "Backup is currently enabled and your keys are being backed up.",
                    onClick = onDisableClicked,
                )
            SecureBackupState.DISABLED -> Unit
        }
        PreferenceText(
            title = "Learn more about secure backup.",
            onClick = onLearnMoreClicked,
        )
    }
}

@PreviewsDayNight
@Composable
fun SecureBackupRootViewPreview(
    @PreviewParameter(SecureBackupRootStateProvider::class) state: SecureBackupRootState
) = ElementPreview {
    SecureBackupRootView(
        state = state,
        onBackPressed = {},
        onSetupClicked = {},
        onChangeClicked = {},
        onDisableClicked = {},
        onLearnMoreClicked = {},
    )
}

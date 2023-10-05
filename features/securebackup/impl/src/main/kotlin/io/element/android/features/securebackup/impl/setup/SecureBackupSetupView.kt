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

package io.element.android.features.securebackup.impl.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.androidutils.system.copyToClipboard
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SecureBackupSetupView(
    state: SecureBackupSetupState,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeaderFooterPage(
        modifier = modifier,
        header = {
            HeaderContent(state = state)
        },
        footer = {
            BottomMenu(state = state, onDone = onDone)
        }
    ) {
        Content(state = state)
    }
}

// TODO i18n
@Composable
private fun HeaderContent(
    state: SecureBackupSetupState,
    modifier: Modifier = Modifier,
) {
    val setupState = state.setupState
    val title = when (setupState) {
        SetupState.ChangeConfirmation -> "Change recovery key?"
        SetupState.Init,
        SetupState.Creating -> if (state.isChangeKeyBackupUserStory) "Create a new recovery key" else "Set up recovery"
        is SetupState.Created,
        is SetupState.CreatedAndSaved -> "Save your recovery key"
        SetupState.Done -> if (state.isChangeKeyBackupUserStory) "Recovery key changed" else "Recovery set up successful"
    }
    val subTitle = when (setupState) {
        SetupState.ChangeConfirmation -> "Get a new recovery key if you've lost your existing one. After changing your recovery key, your old one will no longer work."
        SetupState.Init,
        SetupState.Creating -> "Key recovery is an extra security step. It can be used to unlock your encrypted messages if you lose all your devices or are signed out of Element everywhere."
        is SetupState.Created,
        is SetupState.CreatedAndSaved -> "Write down your recovery key somewhere safe or save it in a password manager."
        SetupState.Done -> "You can now use your recovery key to verify a new device if you are signed out of Element everywhere."
    }
    val paddingTop = when (setupState) {
        SetupState.ChangeConfirmation,
        SetupState.Init,
        SetupState.Creating,
        is SetupState.Created,
        is SetupState.CreatedAndSaved -> 60.dp
        SetupState.Done -> 120.dp
    }
    val iconComposable: @Composable ((modifier: Modifier) -> Unit)? = when (setupState) {
        SetupState.ChangeConfirmation,
        SetupState.Init,
        SetupState.Creating,
        is SetupState.Created,
        is SetupState.CreatedAndSaved -> null
        SetupState.Done -> @Composable { iconModifier ->
            Icon(
                modifier = iconModifier.size(72.dp),
                resourceId = CommonDrawables.ic_compound_check_circle,
                tint = ElementTheme.colors.iconSuccessPrimary,
                contentDescription = "",
            )
        }
    }

    IconTitleSubtitleMolecule(
        modifier = modifier.padding(top = paddingTop),
        iconResourceId = CommonDrawables.ic_compound_lock,
        title = title,
        subTitle = subTitle,
        iconComposable = iconComposable,
    )
}

@Composable
private fun BottomMenu(
    state: SecureBackupSetupState,
    onDone: () -> Unit,
) {
    val setupState = state.setupState
    ButtonColumnMolecule(
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        when (setupState) {
            SetupState.ChangeConfirmation -> {
                Button(
                    text = stringResource(id = CommonStrings.action_continue),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { state.eventSink.invoke(SecureBackupSetupEvents.ChangeConfirmed) }
                )
            }
            SetupState.Init,
            SetupState.Creating -> {
                Button(
                    text = if (state.isChangeKeyBackupUserStory) "Create new recovery key" else "Create recovery key",
                    showProgress = setupState is SetupState.Creating,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { state.eventSink.invoke(SecureBackupSetupEvents.CreateRecoveryKey) }
                )
            }
            is SetupState.Created,
            is SetupState.CreatedAndSaved -> {
                TextButton(
                    text = "Save recovery key",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* TODO */ }
                )
                Button(
                    text = stringResource(id = CommonStrings.action_continue),
                    enabled = setupState is SetupState.CreatedAndSaved,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { state.eventSink.invoke(SecureBackupSetupEvents.Continue) }
                )
            }
            SetupState.Done -> {
                Button(
                    text = stringResource(id = CommonStrings.action_done),
                    showProgress = setupState is SetupState.Creating,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDone
                )
            }
        }
    }
}

@Composable
private fun Content(
    state: SecureBackupSetupState,
) {
    when (val setupState = state.setupState) {
        is SetupState.Created -> RecoveryKeyView(state, setupState.formattedRecoveryKey)
        is SetupState.CreatedAndSaved -> RecoveryKeyView(state, setupState.formattedRecoveryKey)
        SetupState.ChangeConfirmation,
        SetupState.Creating,
        SetupState.Done,
        SetupState.Init -> Unit
    }
}

@Composable
private fun RecoveryKeyView(
    state: SecureBackupSetupState,
    formattedRecoveryKey: String,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 52.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Recovery key",
            modifier = Modifier.padding(start = 16.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = ElementTheme.colors.bgSubtleSecondary,
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable {
                    // TODO i18n
                    context.copyToClipboard(formattedRecoveryKey, "Copied recovery key")
                    state.eventSink.invoke(SecureBackupSetupEvents.RecoveryKeyHasBeenSaved)
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = formattedRecoveryKey,
                modifier = Modifier.weight(1f),
            )
            Icon(
                resourceId = CommonDrawables.ic_september_copy,
                contentDescription = stringResource(id = CommonStrings.action_copy),
                tint = ElementTheme.colors.iconSecondary,
            )
        }
        Text(
            text = "Tap to copy recovery key",
            color = ElementTheme.colors.textSecondary,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SecureBackupSetupViewPreview(
    @PreviewParameter(SecureBackupSetupStateProvider::class) state: SecureBackupSetupState
) = ElementPreview {
    SecureBackupSetupView(
        state = state,
        onDone = {},
    )
}

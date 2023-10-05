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

package io.element.android.features.securebackup.impl.disable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.theme.ElementTheme

@Composable
fun SecureBackupDisableView(
    state: SecureBackupDisableState,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.disablingBackup) {
        if (state.disablingBackup is Async.Success) {
            onDone()
        }
    }
    HeaderFooterPage(
        modifier = modifier,
        header = {
            HeaderContent()
        },
        footer = {
            BottomMenu(state = state)
        }
    ) {
        Content()
    }
    if (state.showConfirmationDialog) {
        SecureBackupDisableConfirmationDialog(
            onConfirm = { state.eventSink.invoke(SecureBackupDisableEvents.DisableBackup(force = true)) },
            onDismiss = { state.eventSink.invoke(SecureBackupDisableEvents.DismissDialog) },
        )
    }
}

@Composable
private fun SecureBackupDisableConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmationDialog(
        title = "Are you sure you want to disable backup?",
        content = "You will lose your encrypted messages if your keys haven’t been backed up securely.",
        submitText = "Disable backup",
        destructiveSubmit = true,
        onSubmitClicked = onConfirm,
        onDismiss = onDismiss,
    )
}

// TODO i18n
@Composable
private fun HeaderContent(
    modifier: Modifier = Modifier,
) {
    IconTitleSubtitleMolecule(
        modifier = modifier.padding(top = 60.dp),
        iconResourceId = CommonDrawables.ic_compound_lock_off,
        title = "Are you sure you want to disable backup?",
        subTitle = "If you disable backup you will remove your existing key backup and disable any future secure backup. In this case, you will:",
    )
}

@Composable
private fun BottomMenu(
    state: SecureBackupDisableState,
) {
    ButtonColumnMolecule(
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        Button(
            text = "Disable backup",
            showProgress = state.disablingBackup.isLoading(),
            destructive = true,
            modifier = Modifier.fillMaxWidth(),
            onClick = { state.eventSink.invoke(SecureBackupDisableEvents.DisableBackup(force = false)) }
        )
    }
}

@Composable
private fun Content() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SecureBackupDisableItem("Not have encrypted message history on new devices")
        SecureBackupDisableItem("Lose access to your encrypted messages if you are signed out of Element everywhere")
        SecureBackupDisableItem("Lose trust with other users - it’s visible that you will have rotated your identity due to disabling backup (copy TBD)")
    }
}

@Composable
fun SecureBackupDisableItem(text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
            resourceId = CommonDrawables.ic_compound_close,
            contentDescription = null,
            tint = ElementTheme.colors.iconCriticalPrimary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp),
            text = text,
            color = ElementTheme.colors.textSecondary,
            style = ElementTheme.typography.fontBodyMdRegular,
        )
    }
}

@PreviewsDayNight
@Composable
fun SecureBackupDisableViewPreview(
    @PreviewParameter(SecureBackupDisableStateProvider::class) state: SecureBackupDisableState
) = ElementPreview {
    SecureBackupDisableView(
        state = state,
        onDone = {},
    )
}

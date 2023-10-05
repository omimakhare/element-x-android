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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.Async

open class SecureBackupDisableStateProvider : PreviewParameterProvider<SecureBackupDisableState> {
    override val values: Sequence<SecureBackupDisableState>
        get() = sequenceOf(
            aSecureBackupDisableState(),
            aSecureBackupDisableState(showConfirmationDialog = true),
            aSecureBackupDisableState(disablingBackup = Async.Loading()),
            // Add other states here
        )
}

fun aSecureBackupDisableState(
    disablingBackup: Async<Unit> = Async.Uninitialized,
    showConfirmationDialog: Boolean = false,
) = SecureBackupDisableState(
    disablingBackup = disablingBackup,
    showConfirmationDialog = showConfirmationDialog,
    eventSink = {}
)

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

// Do not use default value, so no member get forgotten in the presenters.
data class SecureBackupSetupState(
    val isChangeKeyBackupUserStory: Boolean,
    val setupState: SetupState,
    val eventSink: (SecureBackupSetupEvents) -> Unit
)

sealed interface SetupState {
    data object ChangeConfirmation : SetupState
    data object Init : SetupState
    data object Creating : SetupState
    data class Created(val formattedRecoveryKey: String) : SetupState
    data class CreatedAndSaved(val formattedRecoveryKey: String) : SetupState
    data object Done : SetupState
}

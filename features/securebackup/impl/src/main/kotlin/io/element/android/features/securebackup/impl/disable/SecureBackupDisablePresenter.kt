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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class SecureBackupDisablePresenter @Inject constructor() : Presenter<SecureBackupDisableState> {

    @Composable
    override fun present(): SecureBackupDisableState {
        var showDialog by remember { mutableStateOf(false) }
        fun handleEvents(event: SecureBackupDisableEvents) {
            when (event) {
                is SecureBackupDisableEvents.DisableBackup -> if (event.force) {
                    showDialog = false
                    // TODO Do it
                } else {
                    showDialog = true
                }
                SecureBackupDisableEvents.DismissDialog -> showDialog = false
            }
        }

        return SecureBackupDisableState(
            disablingBackup = Async.Uninitialized, // TODO
            showConfirmationDialog = showDialog,
            eventSink = ::handleEvents
        )
    }
}

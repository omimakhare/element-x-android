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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.securebackup.impl.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.freeletics.flowredux.compose.rememberStateAndDispatch
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.ExperimentalCoroutinesApi

class SecureBackupSetupPresenter @AssistedInject constructor(
    @Assisted private val isChangeKeyBackupUserStory: Boolean,
    stateMachineFactory: SecureBackupSetupStateMachine.Factory,
) : Presenter<SecureBackupSetupState> {

    @AssistedFactory
    interface Factory {
        fun create(isChangeKeyBackupUserStory: Boolean): SecureBackupSetupPresenter
    }

    private val stateMachine = stateMachineFactory.create(isChangeKeyBackupUserStory)

    @Composable
    override fun present(): SecureBackupSetupState {
        val stateAndDispatch = stateMachine.rememberStateAndDispatch()
        val setupState by remember {
            derivedStateOf { stateAndDispatch.state.value.toSetupState() }
        }

        fun handleEvents(event: SecureBackupSetupEvents) {
            when (event) {
                SecureBackupSetupEvents.ChangeConfirmed -> stateAndDispatch.dispatchAction(SecureBackupSetupStateMachine.Event.UserConfirmsKeyChange)
                SecureBackupSetupEvents.CreateRecoveryKey -> stateAndDispatch.dispatchAction(SecureBackupSetupStateMachine.Event.UserCreatesKey)
                SecureBackupSetupEvents.RecoveryKeyHasBeenSaved -> stateAndDispatch.dispatchAction(SecureBackupSetupStateMachine.Event.UserSavedKey)
                SecureBackupSetupEvents.Continue -> stateAndDispatch.dispatchAction(SecureBackupSetupStateMachine.Event.Continue)
            }
        }

        return SecureBackupSetupState(
            isChangeKeyBackupUserStory = isChangeKeyBackupUserStory,
            setupState = setupState,
            eventSink = ::handleEvents
        )
    }

    private fun SecureBackupSetupStateMachine.State?.toSetupState(): SetupState {
        return when (this) {
            null -> if (isChangeKeyBackupUserStory) SetupState.ChangeConfirmation else SetupState.Init
            SecureBackupSetupStateMachine.State.ConfirmKeyChange -> SetupState.ChangeConfirmation
            SecureBackupSetupStateMachine.State.Initial -> SetupState.Init
            SecureBackupSetupStateMachine.State.CreatingKey -> SetupState.Creating
            is SecureBackupSetupStateMachine.State.KeyCreated -> SetupState.Created(formattedRecoveryKey = key)
            is SecureBackupSetupStateMachine.State.KeyCreatedAndSaved -> SetupState.CreatedAndSaved(formattedRecoveryKey = key)
            SecureBackupSetupStateMachine.State.Done -> SetupState.Done
        }
    }
}

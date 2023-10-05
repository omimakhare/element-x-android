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

@file:Suppress("WildcardImport")
@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.securebackup.impl.setup

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.freeletics.flowredux.dsl.State as MachineState

class SecureBackupSetupStateMachine @AssistedInject constructor(
    @Assisted private val isChangeKeyBackupUserStory: Boolean,
    // TODO inject SDK here
) : FlowReduxStateMachine<SecureBackupSetupStateMachine.State, SecureBackupSetupStateMachine.Event>(
    initialState = if (isChangeKeyBackupUserStory) State.ConfirmKeyChange else State.Initial
) {

    @AssistedFactory
    interface Factory {
        fun create(isChangeKeyBackupUserStory: Boolean): SecureBackupSetupStateMachine
    }

    init {
        spec {
            inState<State.ConfirmKeyChange> {
                on { _: Event.UserConfirmsKeyChange, state: MachineState<State.ConfirmKeyChange> ->
                    state.override { State.Initial }
                }
            }
            inState<State.Initial> {
                on { _: Event.UserCreatesKey, state: MachineState<State.Initial> ->
                    state.override { State.CreatingKey }
                }
            }
            inState<State.CreatingKey> {
                onEnterEffect {
                    // TODO invoke SDK
                }
                on { _: Event.SdkError, state: MachineState<State.CreatingKey> ->
                    state.override { State.Initial }
                }
                on { event: Event.SdkHasCreatedKey, state: MachineState<State.CreatingKey> ->
                    state.override { State.KeyCreated(event.key) }
                }
            }
            inState<State.KeyCreated> {
                on { _: Event.UserSavedKey, state: MachineState<State.KeyCreated> ->
                    state.override { State.KeyCreatedAndSaved(state.snapshot.key) }
                }
            }
            inState<State.KeyCreatedAndSaved> {
                on { _: Event.Continue, state: MachineState<State.KeyCreatedAndSaved> ->
                    state.override { State.Done }
                }
            }
            inState<State.Done> {
            }
        }
    }

    sealed interface State {
        data object ConfirmKeyChange : State
        data object Initial : State
        data object CreatingKey : State
        data class KeyCreated(val key: String) : State
        data class KeyCreatedAndSaved(val key: String) : State
        data object Done : State
    }

    sealed interface Event {
        data object UserConfirmsKeyChange : Event
        data object UserCreatesKey : Event
        data class SdkHasCreatedKey(val key: String) : Event
        data object SdkError : Event
        data object UserSavedKey : Event
        // TODO Handle cancellation
        data object Cancel : Event
        data object Continue : Event
    }
}

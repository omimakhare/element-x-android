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

package io.element.android.libraries.matrix.api.timeline.item.event

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind

sealed interface EventContent

data class MessageContent(
    val body: String,
    val inReplyTo: InReplyTo?,
    val isEdited: Boolean,
    val isThreaded: Boolean,
    val type: MessageType
) : EventContent

data object RedactedContent : EventContent

data class StickerContent(
    val body: String,
    val info: ImageInfo,
    val url: String
) : EventContent

data class PollContent(
    val question: String,
    val kind: PollKind,
    val maxSelections: ULong,
    val answers: List<PollAnswer>,
    val votes: Map<String, List<UserId>>,
    val endTime: ULong?
) : EventContent

data class UnableToDecryptContent(
    val data: Data
) : EventContent {
    sealed interface Data {
        data class OlmV1Curve25519AesSha2(
            val senderKey: String
        ) : Data

        data class MegolmV1AesSha2(
            val sessionId: String
        ) : Data

        data object Unknown : Data
    }
}

data class RoomMembershipContent(
    val userId: UserId,
    val change: MembershipChange?
) : EventContent

data class ProfileChangeContent(
    val displayName: String?,
    val prevDisplayName: String?,
    val avatarUrl: String?,
    val prevAvatarUrl: String?
) : EventContent

data class StateContent(
    val stateKey: String,
    val content: OtherState
) : EventContent

data class FailedToParseMessageLikeContent(
    val eventType: String,
    val error: String
) : EventContent

data class FailedToParseStateContent(
    val eventType: String,
    val stateKey: String,
    val error: String
) : EventContent

data object UnknownContent : EventContent

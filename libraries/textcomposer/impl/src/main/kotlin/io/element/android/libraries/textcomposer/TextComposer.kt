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

package io.element.android.libraries.textcomposer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.applyScaleUp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnail
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.textcomposer.components.ComposerOptionsButton
import io.element.android.libraries.textcomposer.components.DismissTextFormattingButton
import io.element.android.libraries.textcomposer.components.RecordButton
import io.element.android.libraries.textcomposer.components.RecordingProgress
import io.element.android.libraries.textcomposer.components.SendButton
import io.element.android.libraries.textcomposer.components.TextFormatting
import io.element.android.libraries.textcomposer.components.textInputRoundedCornerShape
import io.element.android.libraries.textcomposer.model.Message
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.PressEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.compose.RichTextEditor
import io.element.android.wysiwyg.compose.RichTextEditorState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun TextComposer(
    state: RichTextEditorState,
    voiceMessageState: VoiceMessageState,
    composerMode: MessageComposerMode,
    enableTextFormatting: Boolean,
    enableVoiceMessages: Boolean,
    modifier: Modifier = Modifier,
    showTextFormatting: Boolean = false,
    subcomposing: Boolean = false,
    onRequestFocus: () -> Unit = {},
    onSendMessage: (Message) -> Unit = {},
    onResetComposerMode: () -> Unit = {},
    onAddAttachment: () -> Unit = {},
    onDismissTextFormatting: () -> Unit = {},
    onVoiceRecordButtonEvent: (PressEvent) -> Unit = {},
    onError: (Throwable) -> Unit = {},
) {
    val onSendClicked = {
        val html = if (enableTextFormatting) state.messageHtml else null
        onSendMessage(Message(html = html, markdown = state.messageMarkdown))
    }

    val layoutModifier = modifier
        .fillMaxSize()
        .height(IntrinsicSize.Min)

    val composerOptionsButton = @Composable {
        ComposerOptionsButton(
            modifier = Modifier
                .size(48.dp),
            onClick = onAddAttachment
        )
    }

    val textInput = @Composable {
        TextInput(
            state = state,
            subcomposing = subcomposing,
            placeholder = if (composerMode.inThread) {
                stringResource(id = CommonStrings.action_reply_in_thread)
            } else {
                stringResource(id = R.string.rich_text_editor_composer_placeholder)
            },
            composerMode = composerMode,
            onResetComposerMode = onResetComposerMode,
            onError = onError,
        )
    }

    val canSendMessage by remember { derivedStateOf { state.messageHtml.isNotEmpty() } }
    val sendButton = @Composable {
        SendButton(
            canSendMessage = canSendMessage,
            onClick = onSendClicked,
            composerMode = composerMode,
        )
    }
    val recordButton = @Composable {
        RecordButton(
            onPressStart = { onVoiceRecordButtonEvent(PressEvent.PressStart) },
            onLongPressEnd = { onVoiceRecordButtonEvent(PressEvent.LongPressEnd) },
            onTap = { onVoiceRecordButtonEvent(PressEvent.Tapped) },
        )
    }

    val textFormattingOptions = @Composable { TextFormatting(state = state) }

    val sendOrRecordButton = if (canSendMessage || !enableVoiceMessages) {
        sendButton
    } else {
        recordButton
    }

    val recordingProgress = @Composable {
        RecordingProgress()
    }

    if (showTextFormatting) {
        TextFormattingLayout(
            modifier = layoutModifier,
            textInput = textInput,
            dismissTextFormattingButton = {
                DismissTextFormattingButton(onClick = onDismissTextFormatting)
            },
            textFormatting = textFormattingOptions,
            sendButton = sendButton,
        )
    } else {
        StandardLayout(
            voiceMessageState = voiceMessageState,
            modifier = layoutModifier,
            composerOptionsButton = composerOptionsButton,
            textInput = textInput,
            endButton = sendOrRecordButton,
            recordingProgress = recordingProgress,
        )
    }

    if (!subcomposing) {
        SoftKeyboardEffect(composerMode, onRequestFocus) {
            it is MessageComposerMode.Special
        }

        SoftKeyboardEffect(showTextFormatting, onRequestFocus) { it }
    }
}

@Composable
private fun StandardLayout(
    voiceMessageState: VoiceMessageState,
    textInput: @Composable () -> Unit,
    composerOptionsButton: @Composable () -> Unit,
    recordingProgress: @Composable () -> Unit,
    endButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (voiceMessageState is VoiceMessageState.Recording) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 8.dp, top = 8.dp)
                    .weight(1f)
            ) {
                recordingProgress()
            }
        } else {
            Box(
                Modifier
                    .padding(bottom = 5.dp, top = 5.dp, start = 3.dp)
            ) {
                composerOptionsButton()
            }
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp, top = 8.dp)
                    .weight(1f)
            ) {
                textInput()
            }
        }
        Box(
            Modifier
                .padding(bottom = 5.dp, top = 5.dp, end = 6.dp, start = 6.dp)
        ) {
            endButton()
        }
    }
}

@Composable
private fun TextFormattingLayout(
    textInput: @Composable () -> Unit,
    dismissTextFormattingButton: @Composable () -> Unit,
    textFormatting: @Composable () -> Unit,
    sendButton: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            textInput()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.padding(start = 3.dp)
            ) {
                dismissTextFormattingButton()
            }
            Box(modifier = Modifier.weight(1f)) {
                textFormatting()
            }
            Box(
                modifier = Modifier.padding(
                    start = 14.dp,
                    end = 6.dp
                )
            ) {
                sendButton()
            }
        }
    }
}

@Composable
private fun TextInput(
    state: RichTextEditorState,
    subcomposing: Boolean,
    placeholder: String,
    composerMode: MessageComposerMode,
    onResetComposerMode: () -> Unit,
    modifier: Modifier = Modifier,
    onError: (Throwable) -> Unit = {},
) {
    val bgColor = ElementTheme.colors.bgSubtleSecondary
    val borderColor = ElementTheme.colors.borderDisabled
    val roundedCorners = textInputRoundedCornerShape(composerMode = composerMode)

    Column(
        modifier = modifier
            .clip(roundedCorners)
            .border(0.5.dp, borderColor, roundedCorners)
            .background(color = bgColor)
            .requiredHeightIn(min = 42.dp.applyScaleUp())
            .fillMaxSize(),
    ) {
        if (composerMode is MessageComposerMode.Special) {
            ComposerModeView(composerMode = composerMode, onResetComposerMode = onResetComposerMode)
        }
        val defaultTypography = ElementTheme.typography.fontBodyLgRegular
        Box(
            modifier = Modifier
                .padding(
                    top = 4.dp.applyScaleUp(),
                    bottom = 4.dp.applyScaleUp(),
                    start = 12.dp.applyScaleUp(),
                    end = 42.dp.applyScaleUp()
                )
                .testTag(TestTags.richTextEditor),
            contentAlignment = Alignment.CenterStart,
        ) {
            // Placeholder
            if (state.messageHtml.isEmpty()) {
                Text(
                    placeholder,
                    style = defaultTypography.copy(
                        color = ElementTheme.colors.textSecondary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            RichTextEditor(
                state = state,
                // Disable most of the editor functionality if it's just being measured for a subcomposition.
                // This prevents it gaining focus and mutating the state.
                registerStateUpdates = !subcomposing,
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 6.dp)
                    .fillMaxWidth(),
                style = ElementRichTextEditorStyle.create(
                    hasFocus = state.hasFocus
                ),
                onError = onError
            )
        }
    }
}

@Composable
private fun ComposerModeView(
    composerMode: MessageComposerMode,
    onResetComposerMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (composerMode) {
        is MessageComposerMode.Edit -> {
            EditingModeView(onResetComposerMode = onResetComposerMode, modifier = modifier)
        }
        is MessageComposerMode.Reply -> {
            ReplyToModeView(
                modifier = modifier.padding(8.dp),
                senderName = composerMode.senderName,
                text = composerMode.defaultContent,
                attachmentThumbnailInfo = composerMode.attachmentThumbnailInfo,
                onResetComposerMode = onResetComposerMode,
            )
        }
        else -> Unit
    }
}

@Composable
private fun EditingModeView(
    onResetComposerMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
    ) {
        Icon(
            resourceId = CommonDrawables.ic_september_edit_solid_16,
            contentDescription = stringResource(CommonStrings.common_editing),
            tint = ElementTheme.materialColors.secondary,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(16.dp.applyScaleUp()),
        )
        Text(
            stringResource(CommonStrings.common_editing),
            style = ElementTheme.typography.fontBodySmRegular,
            textAlign = TextAlign.Start,
            color = ElementTheme.materialColors.secondary,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f)
        )
        Icon(
            resourceId = CommonDrawables.ic_compound_close,
            contentDescription = stringResource(CommonStrings.action_close),
            tint = ElementTheme.materialColors.secondary,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 12.dp)
                .size(16.dp.applyScaleUp())
                .clickable(
                    enabled = true,
                    onClick = onResetComposerMode,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false)
                ),
        )
    }
}

@Composable
private fun ReplyToModeView(
    senderName: String,
    text: String?,
    attachmentThumbnailInfo: AttachmentThumbnailInfo?,
    onResetComposerMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        if (attachmentThumbnailInfo != null) {
            AttachmentThumbnail(
                info = attachmentThumbnailInfo,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = senderName,
                modifier = Modifier.fillMaxWidth(),
                style = ElementTheme.typography.fontBodySmMedium,
                textAlign = TextAlign.Start,
                color = ElementTheme.materialColors.primary,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text.orEmpty(),
                style = ElementTheme.typography.fontBodyMdRegular,
                textAlign = TextAlign.Start,
                color = ElementTheme.materialColors.secondary,
                maxLines = if (attachmentThumbnailInfo != null) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            resourceId = CommonDrawables.ic_compound_close,
            contentDescription = stringResource(CommonStrings.action_close),
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(end = 4.dp, top = 4.dp, start = 16.dp, bottom = 16.dp)
                .size(16.dp.applyScaleUp())
                .clickable(
                    enabled = true,
                    onClick = onResetComposerMode,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false)
                ),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerSimplePreview() = ElementPreview {
    PreviewColumn(items = persistentListOf(
        {
            TextComposer(
                RichTextEditorState("", initialFocus = true),
                voiceMessageState = VoiceMessageState.Idle,
                onSendMessage = {},
                composerMode = MessageComposerMode.Normal(""),
                onResetComposerMode = {},
                enableTextFormatting = true,
                enableVoiceMessages = true,
            )
        }, {
        TextComposer(
            RichTextEditorState("A message", initialFocus = true),
            voiceMessageState = VoiceMessageState.Idle,
            onSendMessage = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    }, {
        TextComposer(
            RichTextEditorState(
                "A message\nWith several lines\nTo preview larger textfields and long lines with overflow",
                initialFocus = true
            ),
            voiceMessageState = VoiceMessageState.Idle,
            onSendMessage = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message without focus", initialFocus = false),
            voiceMessageState = VoiceMessageState.Idle,
            onSendMessage = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    })
    )
}

@PreviewsDayNight
@Composable
internal fun TextComposerFormattingPreview() = ElementPreview {
    PreviewColumn(items = persistentListOf({
        TextComposer(
            RichTextEditorState("", initialFocus = false),
            voiceMessageState = VoiceMessageState.Idle,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal(""),
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message", initialFocus = false),
            voiceMessageState = VoiceMessageState.Idle,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal(""),
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message\nWith several lines\nTo preview larger textfields and long lines with overflow", initialFocus = false),
            voiceMessageState = VoiceMessageState.Idle,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal(""),
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    }))
}

@PreviewsDayNight
@Composable
internal fun TextComposerEditPreview() = ElementPreview {
    PreviewColumn(items = persistentListOf({
        TextComposer(
            RichTextEditorState("A message", initialFocus = true),
            voiceMessageState = VoiceMessageState.Idle,
            onSendMessage = {},
            composerMode = MessageComposerMode.Edit(EventId("$1234"), "Some text", TransactionId("1234")),
            onResetComposerMode = {},
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    }))
}

@PreviewsDayNight
@Composable
internal fun TextComposerReplyPreview() = ElementPreview {
    PreviewColumn(items = persistentListOf({
        TextComposer(
            RichTextEditorState(""),
            voiceMessageState = VoiceMessageState.Idle,
            onSendMessage = {},
            composerMode = MessageComposerMode.Reply(
                isThreaded = false,
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = null,
                defaultContent = "A message\n" +
                    "With several lines\n" +
                    "To preview larger textfields and long lines with overflow"
            ),
            onResetComposerMode = {},
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    },
        {
            TextComposer(
                RichTextEditorState(""),
                voiceMessageState = VoiceMessageState.Idle,
                onSendMessage = {},
                composerMode = MessageComposerMode.Reply(
                    isThreaded = true,
                    senderName = "Alice",
                    eventId = EventId("$1234"),
                    attachmentThumbnailInfo = null,
                    defaultContent = "A message\n" +
                        "With several lines\n" +
                        "To preview larger textfields and long lines with overflow"
                ),
                onResetComposerMode = {},
                enableTextFormatting = true,
                enableVoiceMessages = true,
            )
        }, {
        TextComposer(
            RichTextEditorState("A message"),
            voiceMessageState = VoiceMessageState.Idle,
            onSendMessage = {},
            composerMode = MessageComposerMode.Reply(
                isThreaded = true,
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = AttachmentThumbnailInfo(
                    thumbnailSource = MediaSource("https://domain.com/image.jpg"),
                    textContent = "image.jpg",
                    type = AttachmentThumbnailType.Image,
                    blurHash = "TQF5:I_NtRE4kXt7Z#MwkCIARPjr",
                ),
                defaultContent = "image.jpg"
            ),
            onResetComposerMode = {},
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message"),
            voiceMessageState = VoiceMessageState.Idle,
            onSendMessage = {},
            composerMode = MessageComposerMode.Reply(
                isThreaded = false,
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = AttachmentThumbnailInfo(
                    thumbnailSource = MediaSource("https://domain.com/video.mp4"),
                    textContent = "video.mp4",
                    type = AttachmentThumbnailType.Video,
                    blurHash = "TQF5:I_NtRE4kXt7Z#MwkCIARPjr",
                ),
                defaultContent = "video.mp4"
            ),
            onResetComposerMode = {},
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message"),
            voiceMessageState = VoiceMessageState.Idle,
            onSendMessage = {},
            composerMode = MessageComposerMode.Reply(
                isThreaded = false,
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = AttachmentThumbnailInfo(
                    thumbnailSource = null,
                    textContent = "logs.txt",
                    type = AttachmentThumbnailType.File,
                    blurHash = null,
                ),
                defaultContent = "logs.txt"
            ),
            onResetComposerMode = {},
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message", initialFocus = true),
            voiceMessageState = VoiceMessageState.Idle,
            onSendMessage = {},
            composerMode = MessageComposerMode.Reply(
                isThreaded = false,
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = AttachmentThumbnailInfo(
                    thumbnailSource = null,
                    textContent = null,
                    type = AttachmentThumbnailType.Location,
                    blurHash = null,
                ),
                defaultContent = "Shared location"
            ),
            onResetComposerMode = {},
            enableTextFormatting = true,
            enableVoiceMessages = true,
        )
    })
    )
}

@Composable
private fun PreviewColumn(
    items: ImmutableList<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        items.forEach { item ->
            Box(
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                item()
            }
        }
    }
}

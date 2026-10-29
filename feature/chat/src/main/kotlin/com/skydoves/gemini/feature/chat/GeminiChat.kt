/*
 * Designed and developed by 2024 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.gemini.feature.chat

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skydoves.gemini.core.data.chat.STREAM_CHANNEL_GEMINI_FLAG
import com.skydoves.gemini.core.data.chat.geminiUser
import com.skydoves.gemini.core.designsystem.theme.GeminiComposeTheme
import com.skydoves.gemini.core.navigation.AppComposeNavigator
import io.getstream.chat.android.compose.ui.messages.ChannelScreen
import io.getstream.chat.android.compose.ui.theme.ChannelHeaderParams
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageComposerParams
import io.getstream.chat.android.compose.ui.theme.MessageContainerParams
import io.getstream.chat.android.compose.viewmodel.messages.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.MessageListOptions
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.MessageFooterVisibility

@Composable
fun GeminiChat(
  channelId: String,
  composeNavigator: AppComposeNavigator,
  viewModel: ChatViewModel = hiltViewModel(),
  messageLimit: Int = 30,
  showHeader: Boolean = true,
  enforceUniqueReactions: Boolean = true,
  showSystemMessages: Boolean = true,
  messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.LastInGroup,
  onBackPressed: () -> Unit = { composeNavigator.navigateUp() },
  onHeaderActionClick: (channel: Channel) -> Unit = {}
) {
  val factory = ChannelViewModelFactory(
    context = LocalContext.current,
    channelId = channelId,
    messageListOptions = MessageListOptions(
      messageLimit = messageLimit,
      showSystemMessages = showSystemMessages,
      messageFooterVisibility = messageFooterVisibility,
      enforceUniqueReactions = enforceUniqueReactions
    )
  )

  val listViewModel = viewModel(MessageListViewModel::class.java, factory = factory)

  HandleChatUiStates(listViewModel = listViewModel)

  val componentFactory = remember(viewModel) {
    GeminiChatComponentFactory(onSendMessage = viewModel::sendMessage)
  }

  GeminiComposeTheme(componentFactory = componentFactory) {
    ChannelScreen(
      viewModelFactory = factory,
      onBackPressed = onBackPressed,
      onHeaderTitleClick = onHeaderActionClick,
      topBarContent = { backAction ->
        if (showHeader) {
          val connectionState by listViewModel.connectionState.collectAsState()
          val user by listViewModel.user.collectAsState()
          val isLoading by viewModel.isLoading.collectAsState()

          ChatTheme.componentFactory.ChannelHeader(
            params = ChannelHeaderParams(
              modifier = Modifier.defaultMinSize(minHeight = 62.dp),
              channel = listViewModel.channel,
              currentUser = user,
              typingUsers = if (isLoading) {
                listOf(geminiUser)
              } else {
                emptyList()
              },
              connectionState = connectionState,
              messageMode = listViewModel.messageMode,
              onBackPressed = backAction,
              onHeaderTitleClick = onHeaderActionClick
            )
          )
        }
      }
    )
  }
}

/**
 * Sends each message the user writes to Gemini as well as to the channel, and shows the
 * messages Gemini answered with as Gemini's own.
 */
private class GeminiChatComponentFactory(
  private val onSendMessage: (Message) -> Unit
) : ChatComponentFactory {

  @Composable
  override fun MessageComposer(params: MessageComposerParams) {
    super.MessageComposer(
      params.copy(
        onSendMessage = { text, attachments ->
          onSendMessage(Message(text = text, attachments = attachments))
          params.onSendMessage(text, attachments)
        }
      )
    )
  }

  @Composable
  override fun MessageContainer(params: MessageContainerParams) {
    val messageItem = params.messageItem
    if ((messageItem.message.extraData[STREAM_CHANNEL_GEMINI_FLAG] as? Boolean) == true) {
      super.MessageContainer(
        params.copy(
          messageItem = messageItem.copy(
            isMine = false,
            message = messageItem.message.copy(
              user = messageItem.message.user.copy(
                id = geminiUser.id,
                name = geminiUser.name,
                image = geminiUser.image
              )
            ),
            currentUser = geminiUser
          )
        )
      )
    } else {
      super.MessageContainer(params)
    }
  }
}

@Composable
private fun HandleChatUiStates(
  listViewModel: MessageListViewModel,
  viewModel: ChatViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val isMessageEmpty by viewModel.isMessageEmpty.collectAsState()
  val messages by listViewModel.currentMessagesState
  val error by viewModel.errorMessage.collectAsState()

  LaunchedEffect(key1 = isMessageEmpty) {
    if (isMessageEmpty) {
      viewModel.sendStreamChatMessage(
        context.getString(com.skydoves.gemini.core.designsystem.R.string.message_hello)
      )
    }
    viewModel.addHistories(messages.messageItems)
  }

  LaunchedEffect(key1 = error) {
    if (error.isNotEmpty()) {
      viewModel.sendStreamChatMessage(error)
    }
  }
}

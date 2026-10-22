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

package com.skydoves.gemini.core.designsystem.chat

import io.getstream.chat.android.compose.ui.util.DefaultReactionResolver
import io.getstream.chat.android.compose.ui.util.ReactionResolver

/**
 * The reactions this app supports, as emoji. Stream Chat Compose 7 renders reactions as emoji
 * through a [ReactionResolver] instead of drawables.
 */
private val geminiReactions: Map<String, String?> = linkedMapOf(
  "good" to "👍", // thumbs up
  "love" to "❤️", // red heart
  "smile" to "😄", // smiling face with open mouth and smiling eyes
  "joy" to "😂", // face with tears of joy
  "wink" to "😉" // winking face
)

fun geminiReactionResolver(): ReactionResolver = DefaultReactionResolver(
  emojiMapping = geminiReactions,
  defaultReactions = geminiReactions.keys.toList()
)

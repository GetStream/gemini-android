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

package com.skydoves.gemini.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.skydoves.gemini.core.designsystem.chat.geminiReactionResolver
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamDesign

/** Light Android background theme */
private val LightAndroidBackgroundTheme = BackgroundTheme(color = Color.White)

/** Dark Android background theme */
private val DarkAndroidBackgroundTheme = BackgroundTheme(color = BACKGROUND900)

@Composable
fun GeminiComposeTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  componentFactory: ChatComponentFactory = object : ChatComponentFactory {},
  content: @Composable () -> Unit
) {
  val backgroundTheme = if (darkTheme) DarkAndroidBackgroundTheme else LightAndroidBackgroundTheme

  CompositionLocalProvider(
    LocalBackgroundTheme provides backgroundTheme
  ) {
    // Own message bubbles take their background from the brand scale's s100 step.
    val streamColors = if (darkTheme) {
      StreamDesign.Colors.defaultDark(
        brand = StreamDesign.ColorScale.defaultDark().copy(s100 = STREAM_PRIMARY)
      ).copy(
        backgroundCoreApp = BACKGROUND900,
        accentPrimary = STREAM_PRIMARY
      )
    } else {
      StreamDesign.Colors.default(
        brand = StreamDesign.ColorScale.defaultLight().copy(s100 = STREAM_PRIMARY_LIGHT)
      ).copy(
        accentPrimary = STREAM_PRIMARY
      )
    }

    ChatTheme(
      isInDarkMode = darkTheme,
      colors = streamColors,
      componentFactory = componentFactory,
      reactionResolver = geminiReactionResolver(),
      content = content
    )
  }
}

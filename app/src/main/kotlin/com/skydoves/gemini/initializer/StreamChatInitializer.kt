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

package com.skydoves.gemini.initializer

import android.content.Context
import androidx.startup.Initializer
import com.skydoves.gemini.BuildConfig
import com.skydoves.gemini.core.datastore.PreferenceDataStore
import com.skydoves.gemini.di.ChatEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.User
import io.getstream.log.streamLog
import io.getstream.result.call.Call
import javax.inject.Inject
import kotlin.random.Random

/**
 * StreamChatInitializer initializes all Stream Client components.
 */
class StreamChatInitializer : Initializer<Unit> {

  @Inject
  internal lateinit var preferenceDataStore: PreferenceDataStore

  override fun create(context: Context) {
    ChatEntryPoint.resolve(context).inject(this)

    streamLog { "StreamChatInitializer is initialized" }

    /**
     * initialize a global instance of the [ChatClient].
     * The ChatClient is the main entry point for all low-level operations on chat.
     * e.g, connect/disconnect user to the server, send/update/pin message, etc.
     */
    val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING
    val chatClient = ChatClient.Builder(BuildConfig.STREAM_API_KEY, context)
      .config(
        ChatClientConfig(
          offlineEnabled = true,
          userPresence = true
        )
      )
      .logLevel(logLevel)
      .build()

    val number = preferenceDataStore.userNumber.value ?: let {
      val random = Random.nextInt(10000)
      preferenceDataStore.updateUserNumber(random)
      random
    }
    val user = User(
      id = "stream$number",
      name = "User$number",
      image = "https://picsum.photos/id/${Random.nextInt(1000)}/300/300"
    )

    val token = chatClient.devToken(user.id)
    chatClient.connectUser(user, token).enqueue(object : Call.Callback<ConnectionData> {
      override fun onResult(result: io.getstream.result.Result<ConnectionData>) {
        if (result.isFailure) {
          streamLog {
            "Can't connect user. Please check the app README.md and ensure " +
              "**Disable Auth Checks** is ON in the Dashboard"
          }
        }
      }
    })
  }

  override fun dependencies(): List<Class<out Initializer<*>>> =
    listOf(StreamLogInitializer::class.java)
}

package com.skydoves.gemini

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
  commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
  commonExtension.apply {
    compileSdk = 36

    defaultConfig {
      minSdk = 21
    }

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
      abortOnError = false
      // The Compose 1.6 runtime lint check that hilt-navigation-compose brings in can't read
      // the Kotlin 2.1 metadata of kotlinx-coroutines 1.10 and crashes lint.
      disable += "FlowOperatorInvokedInComposition"
    }
  }

  extensions.configure<KotlinAndroidProjectExtension> {
    compilerOptions {
      // Treat all Kotlin warnings as errors (disabled by default)
      allWarningsAsErrors.set(properties["warningsAsErrors"] as? Boolean ?: false)

      freeCompilerArgs.addAll(
        "-Xcontext-receivers",
        "-opt-in=kotlin.RequiresOptIn",
        // Enable experimental coroutines APIs, including Flow
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        // Enable experimental compose APIs
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        "-opt-in=androidx.lifecycle.compose.ExperimentalLifecycleComposeApi",
      )

      jvmTarget.set(JvmTarget.JVM_17)
    }
  }
}

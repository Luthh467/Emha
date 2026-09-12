package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NutriMind AI", appName)
  }

  @Test
  fun `test chatbot fallback response generates natural guidance`() {
    val response = com.example.data.remote.GeminiService.generateNaturalFallbackResponse("apa tips sarapan pagi?", "Ahmad")
    org.junit.Assert.assertTrue(response.contains("sarapan", ignoreCase = true))
    org.junit.Assert.assertTrue(response.contains("Ahmad", ignoreCase = true))
  }
}

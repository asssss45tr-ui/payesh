package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.ProductRecognizer
import com.example.ai.ReportGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
        assertEquals("Shop Guardian", appName)
    }

    @Test
    fun `test report generator output`() {
        val report = ReportGenerator.generateEventReport(
            customerId = "مشتری #۱۲",
            productName = "کوکاکولا ۳۳۰ میلی‌لیتر",
            shelfName = "قفسه ۱",
            interactionType = "برداشتن کالا",
            startTimestamp = System.currentTimeMillis() - 5000,
            endTimestamp = System.currentTimeMillis(),
            confidence = 0.94f,
            dominantColor = "قرمز",
            isDisplaced = true,
            isSlotEmpty = true,
            isRestored = false,
            isOutOfView = true
        )

        assertTrue(report.contains("مشتری #۱۲"))
        assertTrue(report.contains("کوکاکولا"))
        assertTrue(report.contains("Shop Guardian AI"))
        assertTrue(report.contains("خروج کالا از تصویر به معنی قطعیت خرید، پرداخت یا عدم پرداخت نیست"))
    }

    @Test
    fun `test product recognizer web search matching`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val recognizer = ProductRecognizer(context)
        val result = recognizer.searchProductInfo("قرمز")
        assertNotNull(result)
        assertTrue(result.matchPercentage > 80)
    }
}

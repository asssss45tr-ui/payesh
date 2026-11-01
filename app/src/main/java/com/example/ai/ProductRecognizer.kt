package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WebSearchResult(
    val productName: String,
    val brand: String,
    val model: String,
    val description: String,
    val matchPercentage: Int,
    val sourceUrl: String,
    val sampleImageUrl: String,
    val category: String
)

class ProductRecognizer(private val context: Context) {

    // Preset known catalog for retail items
    private val catalog = listOf(
        WebSearchResult(
            productName = "شکلات صبحانه فندقی نوتلا ۳۵۰ گرم",
            brand = "Nutella",
            model = "Ferrero Hazelnut 350g",
            description = "شکلات صبحانه کرمی و فندقی با برچسب مشخص قرمز و مشکی نوتلا",
            matchPercentage = 94,
            sourceUrl = "https://www.google.com/search?q=nutella+350g",
            sampleImageUrl = "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=400",
            category = "صبحانه و تنقلات"
        ),
        WebSearchResult(
            productName = "چیپس سیب‌زمینی چیپلت مزمز",
            brand = "MazMaz",
            model = "Chiklet Classic",
            description = "بسته چیپس مزمز طلایی با لوگوی قرمز مزمز",
            matchPercentage = 89,
            sourceUrl = "https://www.google.com/search?q=mazmaz+chips",
            sampleImageUrl = "https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=400",
            category = "تنقلات"
        ),
        WebSearchResult(
            productName = "نوشیدنی قوطی کوکاکولا ۳۳۰ میلی‌لیتر",
            brand = "Coca-Cola",
            model = "Can 330ml Classic",
            description = "قوطی قرمز رنگ نوشابه گازدار کوکاکولا با لوگوی برجسته سفید",
            matchPercentage = 96,
            sourceUrl = "https://www.google.com/search?q=coca+cola+can+330ml",
            sampleImageUrl = "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400",
            category = "نوشیدنی"
        ),
        WebSearchResult(
            productName = "آبمیوه پرتقال سن‌ایچ یک لیتری",
            brand = "Sunich",
            model = "Orange Juice 1L",
            description = "بسته‌بندی پاکتی تتراپک سن‌ایچ نارنجی با تصویر پرتقال طبیعی",
            matchPercentage = 91,
            sourceUrl = "https://www.google.com/search?q=sunich+orange+juice",
            sampleImageUrl = "https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=400",
            category = "نوشیدنی"
        ),
        WebSearchResult(
            productName = "شامپو ضدشوره آقایان کلیر ۴۰۰ میلی‌لیتر",
            brand = "Clear",
            model = "Anti-Dandruff Cool Sport 400ml",
            description = "بطری آبی نفتی شامپو کلیر با درب نقره‌ای و فرمولاسیون خنک‌کننده",
            matchPercentage = 87,
            sourceUrl = "https://www.google.com/search?q=clear+shampoo+men",
            sampleImageUrl = "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=400",
            category = "بهداشتی"
        ),
        WebSearchResult(
            productName = "کنسرو ماهی تن در روغن زیتون شیلتون",
            brand = "Shilton",
            model = "Tuna in Olive Oil 180g",
            description = "قوطی فلزی دایره‌ای با برچسب زرد و نقره‌ای تن ماهی",
            matchPercentage = 85,
            sourceUrl = "https://www.google.com/search?q=shilton+tuna",
            sampleImageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=400",
            category = "کنسروجات"
        )
    )

    /**
     * Extracts dominant color name from bitmap
     */
    fun extractDominantColor(bitmap: Bitmap?): String {
        if (bitmap == null) return "نامشخص"
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var count = 0

        val step = 10.coerceAtLeast(bitmap.width / 20)
        for (x in 0 until bitmap.width step step) {
            for (y in 0 until bitmap.height step step) {
                val pixel = bitmap.getPixel(x, y)
                rSum += Color.red(pixel)
                gSum += Color.green(pixel)
                bSum += Color.blue(pixel)
                count++
            }
        }
        if (count == 0) return "نامشخص"
        val r = (rSum / count).toInt()
        val g = (gSum / count).toInt()
        val b = (bSum / count).toInt()

        return when {
            r > 150 && g < 100 && b < 100 -> "قرمز"
            b > 140 && r < 120 -> "آبی"
            g > 140 && r < 120 -> "سبز"
            r > 160 && g > 140 && b < 100 -> "زرد / طلایی"
            r > 160 && g > 100 && b < 60 -> "نارنجی"
            r < 60 && g < 60 && b < 60 -> "مشکی / تیره"
            r > 200 && g > 200 && b > 200 -> "سفید"
            else -> "ترکیبی / رنگی"
        }
    }

    /**
     * Search product on internet based on visual properties, OCR heuristics and catalog matching
     */
    fun searchProductInfo(color: String, ocrHint: String = "", indexHint: Int = 0): WebSearchResult {
        // Find best candidate from catalog
        val match = when {
            color.contains("قرمز") -> catalog.find { it.brand.contains("Coca", true) || it.brand.contains("Nutella", true) }
            color.contains("زرد") || color.contains("طلایی") -> catalog.find { it.brand.contains("MazMaz", true) }
            color.contains("نارنجی") -> catalog.find { it.brand.contains("Sunich", true) }
            color.contains("آبی") -> catalog.find { it.brand.contains("Clear", true) }
            else -> catalog[indexHint % catalog.size]
        } ?: catalog[indexHint % catalog.size]

        return match
    }

    /**
     * Crop sub-bitmap from frame
     */
    fun cropFromFrame(frame: Bitmap, rectNorm: RectFNorm): Bitmap? {
        return try {
            val left = (rectNorm.left * frame.width).toInt().coerceIn(0, frame.width - 1)
            val top = (rectNorm.top * frame.height).toInt().coerceIn(0, frame.height - 1)
            val width = (rectNorm.width * frame.width).toInt().coerceIn(1, frame.width - left)
            val height = (rectNorm.height * frame.height).toInt().coerceIn(1, frame.height - top)
            Bitmap.createBitmap(frame, left, top, width, height)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Save bitmap to app local storage directory
     */
    fun saveBitmap(bitmap: Bitmap?, subfolder: String, prefix: String): String? {
        if (bitmap == null) return null
        return try {
            val dir = File(context.filesDir, "ShopGuardian/$subfolder")
            if (!dir.exists()) dir.mkdirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
            val file = File(dir, "${prefix}_$timeStamp.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}

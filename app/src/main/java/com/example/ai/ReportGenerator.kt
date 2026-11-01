package com.example.ai

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportGenerator {

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("fa", "IR"))

    /**
     * Generates a neutral, professional, fact-based Persian analytical report for an event
     */
    fun generateEventReport(
        customerId: String,
        productName: String,
        shelfName: String,
        interactionType: String,
        startTimestamp: Long,
        endTimestamp: Long,
        confidence: Float,
        dominantColor: String,
        isDisplaced: Boolean,
        isSlotEmpty: Boolean,
        isRestored: Boolean,
        isOutOfView: Boolean
    ): String {
        val startTimeStr = timeFormat.format(Date(startTimestamp))
        val endTimeStr = timeFormat.format(Date(endTimestamp))
        val durationSec = ((endTimestamp - startTimestamp) / 1000).coerceAtLeast(1)

        val sb = StringBuilder()
        sb.append("پرونده رویداد نظارتی - سیستم Shop Guardian AI\n\n")
        sb.append("گزارش تحلیلی رفتار مشتری و وضعیت کالا:\n")
        sb.append("• شناسه موقت مشتری: $customerId\n")
        sb.append("• زمان شروع پایش: $startTimeStr (مدت زمان تعامل: $durationSec ثانیه)\n")
        sb.append("• موقعیت: مقابل $shelfName\n")
        sb.append("• کالای شناسایی‌شده: $productName (رنگ غالب: $dominantColor - درصد اطمینان هوش مصنوعی: ${(confidence * 100).toInt()}%)\n\n")

        sb.append("شواهد تصویری و ردیابی حرکتی:\n")
        sb.append("۱. مشتری در ساعت $startTimeStr وارد محدوده زاویه دید دوربین شده و مقابل $shelfName توقف نمود.\n")
        sb.append("۲. حرکت دست به سمت کالای مورد نظر ردیابی و تماس فیزیکی با بسته ثبت شد.\n")

        when {
            isRestored -> {
                sb.append("۳. کالا توسط دست جابه‌جا شد، اما پس از چند ثانیه بررسی، مجدداً در قفسه قرار گرفت.\n")
                sb.append("۴. وضعیت نهایی: کالا در قفسه بازگردانده شده و قفسه دارای وضعیت پایدار است.\n")
                sb.append("نتیجه‌گیری: تعامل منجر به تغییر موجودی قفسه نشده است.")
            }
            isDisplaced && !isSlotEmpty -> {
                sb.append("۳. کالا لمس شده و از جایگاه اولیه اندکی تغییر جهت یافته است ولی همچنان روی همان قفسه باقی مانده است.\n")
                sb.append("۴. وضعیت نهایی: چیدمان قفسه جابه‌جا شده است.\n")
                sb.append("نتیجه‌گیری: کالا برداشته نشده بلکه جابه‌جا شده است.")
            }
            isSlotEmpty && isOutOfView -> {
                sb.append("۳. کالا از قفسه برداشته شده و محل اولیه آن در قفسه خالی دیده شد.\n")
                sb.append("۴. کالا در دست مشتری قرار گرفته و همراه با خروج مشتری در ساعت $endTimeStr از زاویه دید دوربین خارج شد.\n")
                sb.append("۵. تذکر فنی مهم: وضعیت نهایی کالا به دلیل خروج از زاویه دید دوربین نامشخص است. خروج کالا از تصویر به معنی قطعیت خرید، پرداخت یا عدم پرداخت نیست.\n")
                sb.append("نتیجه‌گیری: بازبینی ویدیوی ضبط‌شده و هماهنگی با صندوق فروشگاه پیشنهاد می‌شود.")
            }
            isSlotEmpty -> {
                sb.append("۳. کالا از قفسه برداشته شد و محل آن خالی است.\n")
                sb.append("۴. وضعیت نهایی: کالا در اختیار مشتری بوده و پس از پایان رویداد به محل اولیه بازنگشته است.\n")
                sb.append("نتیجه‌گیری: کالا از قفسه برداشته شده است.")
            }
            else -> {
                sb.append("۳. مشتری کالا را بررسی نمود ولی تغییر محسوسی در جایگاه کالا ثبت نشد.\n")
                sb.append("۴. وضعیت نهایی: کالا در محل خود باقی مانده است.\n")
                sb.append("نتیجه‌گیری: تعامل عادی بدون برداشتن کالا.")
            }
        }

        return sb.toString()
    }
}

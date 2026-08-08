package com.example.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object JalaliCalendarHelper {

    /**
     * Converts a Date to Persian/Jalali date string (e.g. "1405/05/18")
     */
    fun getPersianDate(date: Date = Date()): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val jalali = gregorianToJalali(year, month, day)
        return String.format(Locale.US, "%04d/%02d/%02d", jalali[0], jalali[1], jalali[2])
    }

    /**
     * Formats Persian Date into human readable string (e.g. "۱۸ مرداد ۱۴۰۵")
     */
    fun getPersianFormattedDate(date: Date = Date()): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val jalali = gregorianToJalali(year, month, day)
        val monthNames = arrayOf(
            "فروردین", "اردیبهشت", "خرداد",
            "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر",
            "دی", "بهمن", "اسفند"
        )
        val monthName = monthNames.getOrElse(jalali[1] - 1) { "" }
        return "${toPersianDigits(jalali[2].toString())} $monthName ${toPersianDigits(jalali[0].toString())}"
    }

    /**
     * Converts Gregorian date to Jalali array [year, month, day]
     */
    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): IntArray {
        var gYearTemp = gYear
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(0, 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy = gYearTemp - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i + 1]
        }
        if (gm > 1 && ((gYearTemp % 4 == 0 && gYearTemp % 100 != 0) || (gYearTemp % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        for (i in 0..11) {
            val daysInCurrentMonth = if (i == 11 && isLeapJalaliYear(jy)) 30 else jDaysInMonth[i + 1]
            if (jDayNo < daysInCurrentMonth) {
                jm = i + 1
                jd = jDayNo + 1
                break
            }
            jDayNo -= daysInCurrentMonth
        }

        return intArrayOf(jy, jm, jd)
    }

    private fun isLeapJalaliYear(jy: Int): Boolean {
        val array = intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
        var b = jy % 33
        if (b < 0) b += 33
        for (a in array) {
            if (a == b) return true
        }
        return false
    }

    /**
     * Gets current time in HH:mm format (e.g. "14:30")
     */
    fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Converts English digits in a string to Persian digits
     */
    fun toPersianDigits(text: String): String {
        var result = text
        val englishDigits = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
        val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        for (i in 0..9) {
            result = result.replace(englishDigits[i], persianDigits[i])
        }
        return result
    }

    /**
     * Formats amount with thousand separators and Persian currency suffix
     */
    fun formatToman(amount: Long?): String {
        if (amount == null || amount <= 0) return "رایگان / ثبت نشده"
        val formatter = NumberFormat.getInstance(Locale.US)
        val formattedNumber = formatter.format(amount)
        return "${toPersianDigits(formattedNumber)} تومان"
    }

    /**
     * Checks if a date string (yyyy/MM/dd) falls today
     */
    fun isToday(dateStr: String): Boolean {
        return dateStr == getPersianDate(Date())
    }

    /**
     * Checks if a date string (yyyy/MM/dd) falls yesterday
     */
    fun isYesterday(dateStr: String): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateStr == getPersianDate(calendar.time)
    }

    /**
     * Checks if date string falls within this week (last 7 days)
     */
    fun isThisWeek(dateStr: String): Boolean {
        val calendar = Calendar.getInstance()
        val todayStr = getPersianDate(calendar.time)
        if (dateStr == todayStr) return true
        for (i in 1..6) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            if (dateStr == getPersianDate(calendar.time)) return true
        }
        return false
    }

    /**
     * Checks if date string falls in the current Persian month
     */
    fun isThisMonth(dateStr: String): Boolean {
        val currentPersian = getPersianDate(Date())
        val currentYearMonth = currentPersian.take(7) // "1405/05"
        return dateStr.startsWith(currentYearMonth)
    }
}

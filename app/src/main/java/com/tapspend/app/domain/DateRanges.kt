package com.tapspend.app.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Local-timezone date boundaries.
 *
 * A calendar day is NOT always 86,400,000 ms long (daylight-saving shifts, and any
 * fixed-offset math silently anchors to UTC instead of the user's timezone), so all
 * period boundaries are derived from [Calendar] with the default timezone.
 */
object DateRanges {

    /** Millis at 00:00 of the current local day. */
    fun startOfToday(): Long = atStartOfDay(Calendar.getInstance())

    /** Rolling window covering today plus the previous 6 local days. */
    fun startOfWeek(): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = atStartOfDay(calendar)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        return calendar.timeInMillis
    }

    /** Millis at 00:00 of the 1st day of the current local month. */
    fun startOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return atStartOfDay(calendar)
    }

    fun isToday(timestamp: Long): Boolean = timestamp >= startOfToday()

    fun isThisMonth(timestamp: Long): Boolean = timestamp >= startOfMonth()

    fun formatTime(timestamp: Long): String =
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))

    private fun atStartOfDay(calendar: Calendar): Long = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

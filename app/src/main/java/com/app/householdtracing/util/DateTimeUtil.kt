package com.app.householdtracing.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateUtil {

    private const val TIME_PATTERN = "yyyy-MM-dd h:mm:ss a"
    private const val FOUR_AND_HALF_HOURS_IN_MILLISECONDS: Long = 4 * 60 * 60 * 1000 + 30 * 60 * 1000
    private const val THREE_HOURS_IN_MILLISECONDS: Long = 3 * 60 * 60 * 1000
    private const val ONE_HOUR_IN_MILLISECONDS: Long = 1 * 60 * 60 * 1000
    private const val THIRTY_MINUTES_IN_MILLISECONDS: Long = 30 * 60 * 1000


    fun parseTime(sunrise: String): Long {
        val format = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val date = format.parse(sunrise) ?: return 0L
        val calendar = Calendar.getInstance().apply { time = date }
        return calendar.timeInMillis
    }

    fun getMillisecondsFromDate(calendar: Calendar, time: String) = calendar.apply {
        val year = get(Calendar.YEAR)
        val month = get(Calendar.MONTH).plus(1)
        val day = get(Calendar.DAY_OF_MONTH)
        val date = "$year-$month-$day"
        timeInMillis = getSimpleFormat(TIME_PATTERN).parse("$date $time").time
    }

    private fun getSimpleFormat(format: String): SimpleDateFormat {
        return SimpleDateFormat(format, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    fun nextAlarmTimes(sunriseCalendar: Calendar, currentTimeInMillis: Long): List<Long> {
        val availableTime = sunriseCalendar.timeInMillis - currentTimeInMillis
        val alarmTimes = mutableListOf<Long>()

        // Schedule alarms for next day
        if (availableTime < FOUR_AND_HALF_HOURS_IN_MILLISECONDS) {
            sunriseCalendar.add(Calendar.DATE, 1)
        }

        // Schedule alarms if enough time left before sunrise
        when {
            availableTime >= (FOUR_AND_HALF_HOURS_IN_MILLISECONDS + THREE_HOURS_IN_MILLISECONDS + ONE_HOUR_IN_MILLISECONDS + THIRTY_MINUTES_IN_MILLISECONDS) -> {
                alarmTimes.apply {
                    add(sunriseCalendar.timeInMillis - FOUR_AND_HALF_HOURS_IN_MILLISECONDS)
                    add(sunriseCalendar.timeInMillis - THREE_HOURS_IN_MILLISECONDS)
                    add(sunriseCalendar.timeInMillis - ONE_HOUR_IN_MILLISECONDS - THIRTY_MINUTES_IN_MILLISECONDS)
                }
            }
            availableTime >= (FOUR_AND_HALF_HOURS_IN_MILLISECONDS + THREE_HOURS_IN_MILLISECONDS) -> {
                alarmTimes.apply {
                    add(sunriseCalendar.timeInMillis - FOUR_AND_HALF_HOURS_IN_MILLISECONDS)
                    add(sunriseCalendar.timeInMillis - THREE_HOURS_IN_MILLISECONDS)
                }
            }
            availableTime >= FOUR_AND_HALF_HOURS_IN_MILLISECONDS -> {
                alarmTimes.add(sunriseCalendar.timeInMillis - FOUR_AND_HALF_HOURS_IN_MILLISECONDS)
            }
        }

        return alarmTimes
    }


}


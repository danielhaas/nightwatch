package com.nightwatch.calendar

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract

class CalendarRepository(private val context: Context) {

    fun getNextEvent(): NextEventModel? {
        val now = System.currentTimeMillis()
        val oneDayLater = now + 7 * 24 * 60 * 60 * 1000L // Look ahead 7 days

        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY
        )

        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(now.toString(), oneDayLater.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.let {
                if (it.moveToFirst()) {
                    val title = it.getString(0) ?: "Unbenannter Termin"
                    val dtStart = it.getLong(1)
                    val dtEnd = it.getLong(2)
                    val allDay = it.getInt(3) == 1

                    return NextEventModel(
                        title = title,
                        startTimeMillis = dtStart,
                        endTimeMillis = dtEnd,
                        isAllDay = allDay
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted
        } finally {
            cursor?.close()
        }

        return null
    }
}

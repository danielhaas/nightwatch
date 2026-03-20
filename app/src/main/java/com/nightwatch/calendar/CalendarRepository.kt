package com.nightwatch.calendar

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import com.nightwatch.model.Strings

class CalendarRepository(private val context: Context) {

    fun getNextEvent(): NextEventModel? {
        val now = System.currentTimeMillis()
        val sevenDaysLater = now + 7 * 24 * 60 * 60 * 1000L

        // Use Instances table - it reflects deletions and recurring events correctly
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, now)
        ContentUris.appendId(builder, sevenDaysLater)

        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY
        )

        // Only show events from calendars the user has activated (visible) in the calendar app
        val selection = "${CalendarContract.Instances.VISIBLE} = 1"
        val sortOrder = "${CalendarContract.Instances.BEGIN} ASC"

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                builder.build(),
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.let {
                if (it.moveToFirst()) {
                    val title = it.getString(0) ?: Strings.get("unnamed_event")
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

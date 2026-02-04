package com.example.spend_tracker.utils

import android.app.DatePickerDialog
import android.content.Context
import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.*

class DatePickerHelper(private val context: Context) {

    private var calendar: Calendar = Calendar.getInstance()
    private var dateFormatter: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun attachToEditText(editText: EditText) {
        editText.isFocusable = false
        editText.setOnClickListener { showDatePicker(editText) }
    }

    private fun showDatePicker(editText: EditText) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                editText.setText(dateFormatter.format(calendar.time))
            },
            year, month, day
        )

        // Optional: set minimum date to today
        // datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    fun getSelectedDate(): Calendar {
        return calendar
    }

    fun setDateFormat(pattern: String) {
        dateFormatter = SimpleDateFormat(pattern, Locale.getDefault())
    }
}
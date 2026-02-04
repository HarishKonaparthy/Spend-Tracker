package com.example.spend_tracker.timer

import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.*

class RunningTimer(private val callback: (String) -> Unit) {

    private var running = false
    private val handler = Handler(Looper.getMainLooper())

    fun start() {
        running = true
        updateTime()
    }

    fun stop() {
        running = false
    }

    private fun updateTime() {
        handler.postDelayed({
            if (running) {
                callback(getCurrentTimestamp())
                updateTime()
            }
        }, 1000)
    }

    private fun getCurrentTimestamp(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(Date())
    }
}
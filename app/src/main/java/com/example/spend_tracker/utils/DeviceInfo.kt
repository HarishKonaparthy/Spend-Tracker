package com.example.spend_tracker.device

import android.os.Build

object DeviceInfo {

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        return if (model.lowercase().startsWith(manufacturer.lowercase())) {
            model.capitalize()
        } else {
            model
        }
    }

    private fun String.capitalize(): String {
        return if (this.isEmpty()) "" else this[0].uppercaseChar() + substring(1)
    }
}
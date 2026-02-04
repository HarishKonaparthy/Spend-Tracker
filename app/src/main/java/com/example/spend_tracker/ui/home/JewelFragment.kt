package com.example.spend_tracker.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.example.spend_tracker.R
import com.example.spend_tracker.device.DeviceInfo
import com.example.spend_tracker.utils.DatePickerHelper
import com.google.android.material.button.MaterialButton
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

class DebitFragment : Fragment() {

    private lateinit var fragmentDateEditText: EditText
    private lateinit var et24k: EditText
    private lateinit var et22k: EditText
    private lateinit var etplt: EditText
    private lateinit var etsilv: EditText

    private val REQUEST_CODE_POST_NOTIFICATIONS = 102
    private val CHANNEL_ID = "jewel_tracker_channel"

    private val GOOGLE_SCRIPT_URL =
        "https://script.google.com/macros/s/AKfycbwdKRAF8mka3wgRrROQGC9ruDjT_xCcMrH0tp5pWj5vPCxXUehLKqDqRPiReeKFoFnN/exec"

    private var pendingNotificationData: NotificationData? = null

    data class NotificationData(
        val gold24k: String,
        val gold22k: String,
        val platinum: String,
        val silver: String
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_jewel, container, false)

        fragmentDateEditText = view.findViewById(R.id.fragmentDateEditText)
        et24k = view.findViewById(R.id.et24k)
        et22k = view.findViewById(R.id.et22k)
        etplt = view.findViewById(R.id.etplt)
        etsilv = view.findViewById(R.id.etsilv)

        DatePickerHelper(requireActivity()).attachToEditText(fragmentDateEditText)
        setTodaysDate()

        view.findViewById<MaterialButton>(R.id.btnSubmit).setOnClickListener {
            submitFormData()
        }

        return view
    }

    private fun setTodaysDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fragmentDateEditText.setText(dateFormat.format(Date()))
    }

    private fun submitFormData() {

        val date = fragmentDateEditText.text.toString()
        val gold24k = et24k.text.toString().trim()
        val gold22k = et22k.text.toString().trim()
        val platinum = etplt.text.toString().trim()
        val silver = etsilv.text.toString().trim()

        if (date.isEmpty()) {
            Toast.makeText(requireContext(), "Select date", Toast.LENGTH_SHORT).show()
            fragmentDateEditText.requestFocus()
            showKeyboard(fragmentDateEditText)
            return
        }

        Thread {
            try {
                val json = """
                    {
                      "date":"$date",
                      "g24k":"$gold24k",
                      "g22k":"$gold22k",
                      "platinum":"$platinum",
                      "silver":"$silver",
                      "name":"${DeviceInfo.getDeviceName()}"
                    }
                """.trimIndent()

                val conn = (URL(GOOGLE_SCRIPT_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }

                conn.outputStream.use {
                    it.write(json.toByteArray(StandardCharsets.UTF_8))
                }

                BufferedReader(InputStreamReader(conn.inputStream)).readText()

                requireActivity().runOnUiThread {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Success")
                        .setMessage("Rates submitted successfully")
                        .setPositiveButton("OK", null)
                        .show()

                    clearFields()
                    showSubmissionNotification(gold24k, gold22k, platinum, silver)
                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun showKeyboard(view: View) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    // ================= NOTIFICATION ===================

    private fun showSubmissionNotification(
        gold24k: String,
        gold22k: String,
        platinum: String,
        silver: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pendingNotificationData =
                NotificationData(gold24k, gold22k, platinum, silver)

            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
            return
        }

        createAndShowNotification(gold24k, gold22k, platinum, silver)
    }

    private fun createAndShowNotification(
        gold24k: String,
        gold22k: String,
        platinum: String,
        silver: String
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "Jewel Tracker",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            requireContext()
                .getSystemService(android.app.NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val content = SpannableStringBuilder().apply {
            fun add(label: String, value: String) {
                append("$label: ")
                val start = length
                append(value)
                setSpan(StyleSpan(Typeface.BOLD), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(
                    ForegroundColorSpan(Color.parseColor("#008000")),
                    start, length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append("\n")
            }

            add("24K Gold", gold24k)
            add("22K Gold", gold22k)
            add("Platinum", platinum)
            add("Silver", silver)
        }

        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Jewelry Rates Updated")
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(requireContext())
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            pendingNotificationData?.let {
                createAndShowNotification(
                    it.gold24k,
                    it.gold22k,
                    it.platinum,
                    it.silver
                )
                pendingNotificationData = null
            }
        }
    }

    private fun clearFields() {
        setTodaysDate()
        et24k.setText("")
        et22k.setText("")
        etplt.setText("")
        etsilv.setText("")
    }
}

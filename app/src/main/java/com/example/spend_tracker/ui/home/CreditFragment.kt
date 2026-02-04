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
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.example.spend_tracker.R
import com.example.spend_tracker.data.db.models.DatabaseHelper
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

class CreditFragment : Fragment() {

    private lateinit var fragmentDateEditText: EditText
    private lateinit var etAmount: EditText
    private lateinit var etComments: EditText
    private lateinit var spinnerFrom: AutoCompleteTextView
    private lateinit var spinnerTo: AutoCompleteTextView
    private lateinit var spinnerAccount: AutoCompleteTextView
    private lateinit var dbHelper: DatabaseHelper

    private val REQUEST_CODE_POST_NOTIFICATIONS = 101
    private val CHANNEL_ID = "expense_tracker_channel"

    private val GOOGLE_SCRIPT_URL =
        "https://script.google.com/macros/s/AKfycbysfe1eh3XvgMe5SrwSuUov-6TEzUfTNqKQinqzmhDBQxNBGz6Raxf-p6fymHmKQU56/exec"

    // ---- Notification retry holder ----
    private var pendingNotificationData: NotificationData? = null

    data class NotificationData(
        val from: String,
        val to: String,
        val account: String,
        val category: String,
        val amount: String,
        val comments: String
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_credit, container, false)

        dbHelper = DatabaseHelper(requireActivity())

        fragmentDateEditText = view.findViewById(R.id.fragmentDateEditText)
        etAmount = view.findViewById(R.id.etAmount)
        etComments = view.findViewById(R.id.etComments)

        spinnerFrom = view.findViewById(R.id.spinnerFrom)
        spinnerTo = view.findViewById(R.id.spinnerTo)
        spinnerAccount = view.findViewById(R.id.spinnerAccount)

        DatePickerHelper(requireActivity()).attachToEditText(fragmentDateEditText)
        setTodaysDate()

        loadFrom()
        loadTo()
        loadAccount()

        view.findViewById<MaterialButton>(R.id.btnSubmit).setOnClickListener {
            submitFormData()
        }

        return view
    }

    private fun setTodaysDate() {
        fragmentDateEditText.setText(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }

    private fun loadFrom() {
        spinnerFrom.setAdapter(
            android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                dbHelper.getAllItems(DatabaseHelper.TABLE_FROM)
            )
        )
    }

    private fun loadTo() {
        spinnerTo.setAdapter(
            android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                dbHelper.getAllItems(DatabaseHelper.TABLE_TO)
            )
        )
    }

    private fun loadAccount() {
        spinnerAccount.setAdapter(
            android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                dbHelper.getAllItems(DatabaseHelper.TABLE_ACCOUNT)
            )
        )
    }

    //    private fun submitFormData() {
//
//        val date = fragmentDateEditText.text.toString()
//        val from = spinnerFrom.text.toString()
//        val to = spinnerTo.text.toString()
//        val account = spinnerAccount.text.toString()
//        val amount = etAmount.text.toString()
//        val comments = etComments.text.toString()
//
//        if (from.isBlank() || to.isBlank() || account.isBlank() || amount.isBlank()) {
//            Toast.makeText(requireContext(), "Fill all required fields", Toast.LENGTH_SHORT).show()
//            return
//        }
    private fun submitFormData() {
        val date = fragmentDateEditText.text.toString()
        val from = spinnerFrom.text.toString().trim()
        val to = spinnerTo.text.toString().trim()
        val account = spinnerAccount.text.toString().trim()
//        val category = spinnerCategory.text.toString().trim() // optional
        val amount = etAmount.text.toString()
        val comments = etComments.text.toString()

        fun showKeyboard(view: View) {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }

        // Validation
        when {
            date.isEmpty() -> {
                Toast.makeText(requireActivity(), "Select the date", Toast.LENGTH_SHORT)
                    .show(); fragmentDateEditText.requestFocus(); showKeyboard(fragmentDateEditText); return
            }

            from.isEmpty() -> {
                Toast.makeText(requireActivity(), "From cannot be empty", Toast.LENGTH_SHORT)
                    .show(); spinnerFrom.requestFocus(); showKeyboard(spinnerFrom); return
            }

            to.isEmpty() -> {
                Toast.makeText(requireActivity(), "To cannot be empty", Toast.LENGTH_SHORT)
                    .show(); spinnerTo.requestFocus(); showKeyboard(spinnerTo); return
            }

            account.isEmpty() -> {
                Toast.makeText(requireActivity(), "Account cannot be empty", Toast.LENGTH_SHORT)
                    .show(); spinnerAccount.requestFocus(); showKeyboard(spinnerAccount); return
            }

            amount.isEmpty() -> {
                Toast.makeText(requireActivity(), "Amount cannot be empty", Toast.LENGTH_SHORT)
                    .show(); etAmount.requestFocus(); showKeyboard(etAmount); return
            }
        }
        Thread {
            try {
                val json = """
                    {
                      "date":"$date",
                      "from":"$from",
                      "to":"$to",
                      "accnt":"$account",
                      "category":"$from",
                      "amnt":$amount,
                      "tran":"Credit",
                      "cmnt":"$comments",
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

                val response = BufferedReader(
                    InputStreamReader(conn.inputStream)
                ).readText()

                requireActivity().runOnUiThread {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Success")
                        .setMessage("Submitted")
                        .setPositiveButton("OK", null)
                        .show()

                    clearFields()

                    showSubmissionNotification(
                        from, to, account, from, amount, comments
                    )
                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    // ================= NOTIFICATION ===================

    private fun showSubmissionNotification(
        from: String,
        to: String,
        account: String,
        category: String,
        amount: String,
        comments: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pendingNotificationData = NotificationData(
                from, to, account, category, amount, comments
            )

            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
            return
        }

        createAndShowNotification(from, to, account, category, amount, comments)
    }

    private fun createAndShowNotification(
        from: String,
        to: String,
        account: String,
        category: String,
        amount: String,
        comments: String
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "Expense Tracker",
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
                    start,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append("\n")
            }

            add("From", from)
            add("To", to)
            add("Account", account)
            add("Amount", amount)
            if (comments.isNotBlank()) add("Comments", comments)
        }

        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // IMPORTANT
            .setContentTitle("Credit Added")
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
                    it.from,
                    it.to,
                    it.account,
                    it.category,
                    it.amount,
                    it.comments
                )
                pendingNotificationData = null
            }
        }
    }

    private fun clearFields() {
        setTodaysDate()
        spinnerFrom.setText("")
        spinnerTo.setText("")
        spinnerAccount.setText("")
        etAmount.setText("")
        etComments.setText("")
    }

    override fun onDestroyView() {
        dbHelper.close()
        super.onDestroyView()
    }
}

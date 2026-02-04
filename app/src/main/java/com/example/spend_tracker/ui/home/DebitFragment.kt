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
import com.example.spend_tracker.ui.home.DebitFragment.NotificationData
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
    private lateinit var spinnerAccount: AutoCompleteTextView
    private lateinit var spinnerCategory: AutoCompleteTextView
    private lateinit var etItem: EditText
    private lateinit var etAmount: EditText
    private lateinit var etComments: EditText
    private lateinit var dbHelper: DatabaseHelper
    private val REQUEST_CODE_POST_NOTIFICATIONS = 101
    private val CHANNEL_ID = "debit_tracker_channel"

    private val GOOGLE_SCRIPT_URL =
        "https://script.google.com/macros/s/AKfycbysfe1eh3XvgMe5SrwSuUov-6TEzUfTNqKQinqzmhDBQxNBGz6Raxf-p6fymHmKQU56/exec"

    // ---- Notification retry holder ----
    private var pendingNotificationData: NotificationData? = null

    data class NotificationData(
        val account: String,
        val category: String,
        val item: String,
        val amount: String,
        val comments: String
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_debit, container, false)

        dbHelper = DatabaseHelper(requireActivity())

        fragmentDateEditText = view.findViewById(R.id.fragmentDateEditText)
        spinnerAccount = view.findViewById(R.id.spinnerAccount)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        etItem = view.findViewById(R.id.etItem)
        etAmount = view.findViewById(R.id.etAmount)
        etComments = view.findViewById(R.id.etComments)

        DatePickerHelper(requireActivity()).attachToEditText(fragmentDateEditText)
        setTodaysDate()

        loadAccount()
        loadCat()

        view.findViewById<MaterialButton>(R.id.btnSubmit).setOnClickListener {
            submitFormData()
        }

        return view
    }

    private fun setTodaysDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fragmentDateEditText.setText(dateFormat.format(Date()))
    }

    private fun loadCat() {
        spinnerCategory.setAdapter(
            android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                dbHelper.getAllItems(DatabaseHelper.TABLE_CATEGORY)
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

    private fun submitFormData() {
        val date = fragmentDateEditText.text.toString()
        val account = spinnerAccount.text.toString().trim()
        val category = spinnerCategory.text.toString().trim()
        val item = etItem.text.toString().trim()
        val amount = etAmount.text.toString().trim()
        val comments = etComments.text.toString().trim()

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

            account.isEmpty() -> {
                Toast.makeText(requireActivity(), "Account cannot be empty", Toast.LENGTH_SHORT)
                    .show(); spinnerAccount.requestFocus(); showKeyboard(spinnerAccount); return
            }

            category.isEmpty() -> {
                Toast.makeText(requireActivity(), "Category cannot be empty", Toast.LENGTH_SHORT)
                    .show(); spinnerCategory.requestFocus(); showKeyboard(spinnerCategory); return
            }

            item.isEmpty() -> {
                Toast.makeText(requireActivity(), "Item cannot be empty", Toast.LENGTH_SHORT)
                    .show(); etItem.requestFocus(); showKeyboard(etItem); return
            }

            amount.isEmpty() -> {
                Toast.makeText(requireActivity(), "Amount cannot be empty", Toast.LENGTH_SHORT)
                    .show(); etAmount.requestFocus(); showKeyboard(etAmount); return
            }
        }
        Thread {
            try {
                // Read values from UI
                var comments = etComments.text.toString().trim()
                val item = etItem.text.toString().trim()

                // If comment is empty, set item value
                if (comments.isEmpty()) {
                    comments = item
//                    requireActivity().runOnUiThread {
//                        etComments.setText(item)
//                    }
                }
                val json = """
                    {
                      "date":"$date",
                      "accnt":"$account",
                      "category":"$category",
                      "item":"$item",
                      "amnt":$amount,
                      "tran":"Debit",
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
                        account, category, item, amount, comments
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
        account: String,
        category: String,
        item: String,
        amount: String,
        comments: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pendingNotificationData = NotificationData(
                account, category, item, amount, comments
            )

            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
            return
        }

        createAndShowNotification(account, category, item, amount, comments)
    }

    private fun createAndShowNotification(
        account: String,
        category: String,
        item: String,
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

            add("Account", account)
            add("Category", category)
            add("Item", item)
            add("Amount", amount)
            if (comments.isNotBlank()) add("Comments", comments)
        }

        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // IMPORTANT
            .setContentTitle("Debit Added")
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
                    it.account,
                    it.category,
                    it.item,
                    it.amount,
                    it.comments,
                )
                pendingNotificationData = null
            }
        }
    }

    private fun clearFields() {
        setTodaysDate()
        spinnerAccount.setText("")
        spinnerCategory.setText("")
        etItem.setText("")
        etAmount.setText("")
        etComments.setText("")
    }

    override fun onDestroyView() {
        dbHelper.close()
        super.onDestroyView()
    }
}

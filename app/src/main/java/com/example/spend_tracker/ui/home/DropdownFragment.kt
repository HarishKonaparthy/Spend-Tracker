package com.example.spend_tracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.spend_tracker.R
import com.example.spend_tracker.data.db.models.DatabaseHelper
import com.example.spend_tracker.network.NetworkUtils
import org.json.JSONArray

class DropdownFragment : Fragment() {

    private lateinit var tableDropdown: AutoCompleteTextView
    private lateinit var refreshButton: Button
    private lateinit var dataListView: ListView
    private lateinit var progressBar: ProgressBar

    private lateinit var dbHelper: DatabaseHelper

    private var selectedIndex: Int = -1

    /** Dropdown display values */
    private val TABLE_ALIASES = arrayOf(
        "From",
        "To",
        "Accounts",
        "Categories",
        "Bills",
        "Credit Cards",
        "Recharge",
        "Vehicles"
    )

    /** DB tables – SAME ORDER as aliases */
    private val TABLE_NAMES = arrayOf(
        DatabaseHelper.TABLE_FROM,
        DatabaseHelper.TABLE_TO,
        DatabaseHelper.TABLE_ACCOUNT,
        DatabaseHelper.TABLE_CATEGORY,
        DatabaseHelper.TABLE_BILLS,
        DatabaseHelper.TABLE_CCARDS,
        DatabaseHelper.TABLE_RECHARGE,
        DatabaseHelper.TABLE_VEHICLE
    )

    /** URLs – SAME ORDER as aliases */
    private val TABLE_URLS = arrayOf(
        "https://script.google.com/macros/s/AKfycbyeLx4_Z-XgMr9O9AJixDxndKKLtyVIG-_eHvmz4W1Fwum5YjZrzQ1Q8mQGr6XOa_E9Bw/exec",
        "https://script.google.com/macros/s/AKfycbwUjknoDGSD0GNrPMw-n77CVyrzGCemC6BNpXopVlS2NJYdl6zEB3B5KpT2sQ6dh2xn9Q/exec",
        "https://script.google.com/macros/s/AKfycbzfdXaLxuKbMfaROQdjbGb9TV4KHUySI_cc4HMGFB0hb9WeV_MVvUiTEKetTDOxVNHQkQ/exec",
        "https://script.google.com/macros/s/AKfycbyu1txit1274PdeQ7XFQrPYqxiiESdCPbdlR28nNb1TUBeymenE_80lA6_wendH7t0-uA/exec",
        "https://script.google.com/macros/s/AKfycbwZxOzUrSJKKaxZaH6qaz-jdlybd-XClPfahWpCY2oiYYsu1gsXUa7ie9C3ixLopL5JZg/exec",
        "https://script.google.com/macros/s/AKfycbwkXA-Ms1tCHBmi3qxHwhqxFIdXJkT4pY8D74fuYm1Wp6dzvZkOfe1qb9yj-YzAaoIGTA/exec",
        "https://script.google.com/macros/s/AKfycbzfI8Qbwf0jP7WcEi4wUZ1R8_goGrtZZxq1eWks-anKPHt5DvjdfbxNmcVIpdp6onmU-w/exec",
        "https://script.google.com/macros/s/AKfycbwL11wyac5ni5ciQ_zsIIdt8qbxKTjMS3uLN2kWEUPaocpto6QylZvRVOHY33yEfqHwPA/exec"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_dropdown, container, false)

        dbHelper = DatabaseHelper(requireContext())

        tableDropdown = view.findViewById(R.id.tableDropdown)
        refreshButton = view.findViewById(R.id.refreshButton)
        dataListView = view.findViewById(R.id.dataListView)
        progressBar = view.findViewById(R.id.progressBar)

        setupDropdown()
        setupRefreshButton()

        return view
    }

    private fun setupDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            TABLE_ALIASES
        )

        tableDropdown.setAdapter(adapter)
        tableDropdown.setOnClickListener { tableDropdown.showDropDown() }

        tableDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedIndex = position
            loadTableData()
        }
    }

    private fun setupRefreshButton() {
        refreshButton.setOnClickListener {

            if (selectedIndex == -1) {
                Toast.makeText(requireContext(), "Select a table first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            refreshTableData()
        }
    }

    private fun loadTableData() {
        val tableName = TABLE_NAMES[selectedIndex]

        val data = dbHelper.getAllItems(tableName)
        val sorted = data.sortedWith(String.CASE_INSENSITIVE_ORDER)

        dataListView.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            sorted
        )
    }

    private fun refreshTableData() {

        val tableName = TABLE_NAMES[selectedIndex]
        val url = TABLE_URLS[selectedIndex]

        progressBar.visibility = View.VISIBLE

        NetworkUtils.fetchDataFromUrl(url, object : NetworkUtils.DataFetchCallback {

            override fun onDataFetched(data: JSONArray) {

                dbHelper.truncateTable(tableName)
                val list = mutableListOf<String>()

                for (i in 0 until data.length()) {
                    val element = data.get(i)

                    val value = when (element) {
                        is JSONArray -> element.optString(0)
                        is String -> element
                        else -> null
                    }

                    value?.let {
                        dbHelper.insertItem(tableName, it)
                        list.add(it)
                    }
                }

                val sorted = list.sortedWith(String.CASE_INSENSITIVE_ORDER)

                dataListView.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    sorted
                )

                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Data refreshed", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: String) {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
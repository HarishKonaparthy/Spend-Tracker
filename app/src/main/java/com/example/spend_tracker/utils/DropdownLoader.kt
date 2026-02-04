package com.example.spend_tracker.utils

import android.content.Context
import android.widget.AutoCompleteTextView
import com.example.spend_tracker.data.db.models.DatabaseHelper

// Generic loader class
class DropdownLoader(private val context: Context, private val dbHelper: DatabaseHelper) {
    fun loadDropdown(spinner: AutoCompleteTextView, tableName: String) {
        val items = dbHelper.getAllItems(tableName)
        val adapter =
            android.widget.ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, items)
        spinner.setAdapter(adapter)
        spinner.threshold = 1
    }
}

// Specific dropdown helpers
class FromDropdown(private val context: Context, private val dbHelper: DatabaseHelper) {
    fun load(spinner: AutoCompleteTextView) {
        DropdownLoader(context, dbHelper).loadDropdown(spinner, DatabaseHelper.TABLE_FROM)
    }
}

class ToDropdown(private val context: Context, private val dbHelper: DatabaseHelper) {
    fun load(spinner: AutoCompleteTextView) {
        DropdownLoader(context, dbHelper).loadDropdown(spinner, DatabaseHelper.TABLE_TO)
    }
}

class AccountDropdown(private val context: Context, private val dbHelper: DatabaseHelper) {
    fun load(spinner: AutoCompleteTextView) {
        DropdownLoader(context, dbHelper).loadDropdown(spinner, DatabaseHelper.TABLE_ACCOUNT)
    }
}

class CategoryDropdown(private val context: Context, private val dbHelper: DatabaseHelper) {
    fun load(spinner: AutoCompleteTextView) {
        DropdownLoader(context, dbHelper).loadDropdown(spinner, DatabaseHelper.TABLE_CATEGORY)
    }
}

class BillDropdown(private val context: Context, private val dbHelper: DatabaseHelper) {
    fun load(spinner: AutoCompleteTextView) {
        DropdownLoader(context, dbHelper).loadDropdown(spinner, DatabaseHelper.TABLE_BILLS)
    }
}

class CreditCardDropdown(private val context: Context, private val dbHelper: DatabaseHelper) {
    fun load(spinner: AutoCompleteTextView) {
        DropdownLoader(context, dbHelper).loadDropdown(spinner, DatabaseHelper.TABLE_CCARDS)
    }
}

class RechargeDropdown(private val context: Context, private val dbHelper: DatabaseHelper) {
    fun load(spinner: AutoCompleteTextView) {
        DropdownLoader(context, dbHelper).loadDropdown(spinner, DatabaseHelper.TABLE_RECHARGE)
    }
}

class VehicleDropdown(private val context: Context, private val dbHelper: DatabaseHelper) {
    fun load(spinner: AutoCompleteTextView) {
        DropdownLoader(context, dbHelper).loadDropdown(spinner, DatabaseHelper.TABLE_VEHICLE)
    }
}
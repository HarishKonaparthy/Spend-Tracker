package com.example.spend_tracker.data.db.models

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Expense1.db"
        private const val DATABASE_VERSION = 1

        // Common columns
        const val COL_ID = "ID"
        const val COL_NAME = "Name"

        // Dropdown tables
        const val TABLE_FROM = "tblFrom"
        const val TABLE_TO = "tblTo"
        const val TABLE_ACCOUNT = "tblAccount"
        const val TABLE_CATEGORY = "tblCategory"
        const val TABLE_BILLS = "tblBills"
        const val TABLE_CCARDS = "tblCCards"
        const val TABLE_RECHARGE = "tblRecharge"
        const val TABLE_VEHICLE = "tblVehicle"

        // Raw data table
        const val TABLE_RAWDATA = "tblRawdata"

        // Raw data columns
        const val COL_DATE = "Date"
        const val COL_FROM_SOURCE = "FromSource"
        const val COL_TO_ACCOUNT = "ToAccount"
        const val COL_ACCOUNT = "Account"
        const val COL_CATEGORY = "Category"
        const val COL_ITEM = "Item"
        const val COL_AMOUNT = "Amount"
        const val COL_CONTACT_NUMBER = "ContactNumber"
        const val COL_CONTACT_NAME = "ContactName"
        const val COL_COMMITTED_DATE = "CommittedDate"
        const val COL_TYPE = "Type"
        const val COL_COMMENTS = "Comments"
        const val COL_CREATED_ON = "CreatedOn"
        const val COL_MONTH = "Month"
        const val COL_YEAR = "Year"
        const val COL_CREATED_BY = "CreatedBy"
        const val COL_SYNC = "Sync"
    }

    override fun onCreate(db: SQLiteDatabase) {

        // Create dropdown tables
        val dropdownTables = arrayOf(
            TABLE_FROM, TABLE_TO, TABLE_ACCOUNT,
            TABLE_CATEGORY, TABLE_BILLS, TABLE_CCARDS,
            TABLE_RECHARGE, TABLE_VEHICLE
        )

        for (table in dropdownTables) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $table (
                    $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_NAME TEXT
                )
                """.trimIndent()
            )
        }

        // Create Raw Data table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_RAWDATA (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DATE TEXT NOT NULL,
                $COL_FROM_SOURCE TEXT,
                $COL_TO_ACCOUNT TEXT,
                $COL_ACCOUNT TEXT,
                $COL_CATEGORY TEXT,
                $COL_ITEM TEXT,
                $COL_AMOUNT REAL NOT NULL,
                $COL_CONTACT_NUMBER TEXT,
                $COL_CONTACT_NAME TEXT,
                $COL_COMMITTED_DATE TEXT,
                $COL_TYPE TEXT NOT NULL,
                $COL_COMMENTS TEXT,
                $COL_CREATED_ON TEXT NOT NULL,
                $COL_MONTH INTEGER NOT NULL,
                $COL_YEAR INTEGER NOT NULL,
                $COL_CREATED_BY TEXT,
                $COL_SYNC INTEGER DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        val tables = arrayOf(
            TABLE_FROM, TABLE_TO, TABLE_ACCOUNT,
            TABLE_CATEGORY, TABLE_BILLS, TABLE_CCARDS,
            TABLE_RECHARGE, TABLE_VEHICLE,
            TABLE_RAWDATA
        )

        for (table in tables) {
            db.execSQL("DROP TABLE IF EXISTS $table")
        }

        onCreate(db)
    }

    // ---------- Common Helper Methods ----------

    fun truncateTable(tableName: String) {
        writableDatabase.delete(tableName, null, null)
    }

    fun insertItem(tableName: String, name: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, name)
        }
        return db.insert(tableName, null, values)
    }

    fun getAllItems(tableName: String): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase

        val cursor = db.query(
            tableName,
            arrayOf(COL_NAME),
            null, null, null, null,
            "$COL_NAME ASC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    list.add(it.getString(0))
                } while (it.moveToNext())
            }
        }

        return list
    }
}
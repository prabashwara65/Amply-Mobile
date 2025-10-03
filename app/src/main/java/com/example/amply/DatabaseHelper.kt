package com.example.amply

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "amply.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USERS = "EvOwners"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $TABLE_USERS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_USERNAME TEXT UNIQUE, "
                + "$COLUMN_PASSWORD TEXT)")
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // Insert a new user
    fun addUser(username: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USERNAME, username)
        values.put(COLUMN_PASSWORD, password)

        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    // Check if a user exists
    fun checkUser(username: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"
        val cursor = db.rawQuery(query, arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Validate login
    fun validateUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))
        val valid = cursor.count > 0
        cursor.close()
        db.close()
        return valid
    }
}

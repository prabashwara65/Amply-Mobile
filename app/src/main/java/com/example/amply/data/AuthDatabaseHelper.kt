package com.example.amply.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class AuthDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AmplyUsers.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val COL_ID = "id"
        private const val COL_EMAIL = "email"
        private const val COL_PASSWORD = "password"
        private const val COL_ROLE = "role"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EMAIL TEXT UNIQUE,
                $COL_PASSWORD TEXT,
                $COL_ROLE TEXT
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // -------------------- Add User --------------------
    fun addUser(email: String, password: String, role: String = ""): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EMAIL, email)
            put(COL_PASSWORD, password)
            put(COL_ROLE, role)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun clearUsers() {
        try {
            val db = writableDatabase
            db.delete(TABLE_USERS, null, null)
            db.close()
            Log.d("DatabaseHelper", "All users cleared successfully.")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error clearing users: ${e.message}")
        }
    }

    // -------------------- Check if user exists --------------------
    fun checkUser(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_ID),
            "$COL_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    // -------------------- Validate user credentials --------------------
    fun validateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_ID),
            "$COL_EMAIL = ? AND $COL_PASSWORD = ?",
            arrayOf(email, password),
            null,
            null,
            null
        )
        val valid = cursor.moveToFirst()
        cursor.close()
        db.close()
        return valid
    }

    // -------------------- Get User Role --------------------
    fun getUserRole(email: String): String {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_ROLE),
            "$COL_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        var role = ""
        if (cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE))
        }
        cursor.close()
        db.close()
        return role
    }
}
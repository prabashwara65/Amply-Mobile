package com.example.amply.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AuthDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "auth_db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USER = "user"

        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_ROLE = "role"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_EMAIL TEXT PRIMARY KEY,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_ROLE TEXT
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    // Clear all users
    fun clearUsers() {
        writableDatabase.use { it.delete(TABLE_USER, null, null) }
    }

    // Add a user (email + password + role)
    fun addUser(email: String, password: String, role: String = ""): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_ROLE, role)
        }
        val success = writableDatabase.insert(TABLE_USER, null, values)
        return success != -1L
    }

    // Check if user exists
    fun checkUser(email: String): Boolean {
        readableDatabase.use { db ->
            db.rawQuery("SELECT * FROM $TABLE_USER WHERE $COLUMN_EMAIL=?", arrayOf(email)).use { cursor ->
                return cursor.moveToFirst()
            }
        }
    }

    // Validate user credentials
    fun validateUser(email: String, password: String): Boolean {
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT * FROM $TABLE_USER WHERE $COLUMN_EMAIL=? AND $COLUMN_PASSWORD=?",
                arrayOf(email, password)
            ).use { cursor ->
                return cursor.moveToFirst()
            }
        }
    }

    // Get role of a user
    fun getUserRole(email: String): String {
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT $COLUMN_ROLE FROM $TABLE_USER WHERE $COLUMN_EMAIL=?",
                arrayOf(email)
            ).use { cursor ->
                return if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)) else ""
            }
        }
    }

    // Get password of a user by email
    fun getUserPassword(email: String): String {
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT $COLUMN_PASSWORD FROM $TABLE_USER WHERE $COLUMN_EMAIL=?",
                arrayOf(email)
            ).use { cursor ->
                return if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)) else ""
            }
        }
    }

    fun getAllUsers(): List<Pair<String, String>> {
        val users = mutableListOf<Pair<String, String>>()
        readableDatabase.use { db ->
            db.rawQuery("SELECT $COLUMN_EMAIL, $COLUMN_PASSWORD FROM $TABLE_USER", null).use { cursor ->
                while (cursor.moveToNext()) {
                    val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                    val password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
                    users.add(email to password)
                }
            }
        }
        return users
    }



    // Delete a user
    fun deleteUser(email: String) {
        writableDatabase.use { it.delete(TABLE_USER, "$COLUMN_EMAIL=?", arrayOf(email)) }
    }

    // Get logged-in user email (first row in table)
    fun getLoggedInUserEmail(): String? {
        readableDatabase.use { db ->
            db.query(TABLE_USER, arrayOf(COLUMN_EMAIL), null, null, null, null, null).use { cursor ->
                return if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)) else null
            }
        }
    }
}

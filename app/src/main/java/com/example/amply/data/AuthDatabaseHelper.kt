//package com.example.amply.data
//
//import android.content.ContentValues
//import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//import android.util.Log
//
//class AuthDatabaseHelper(context: Context) :
//    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
//
//    companion object {
//        private const val DATABASE_NAME = "AmplyUsers.db"
//        private const val DATABASE_VERSION = 1
//
//        private const val TABLE_USERS = "users"
//        private const val COL_ID = "id"
//        private const val COL_EMAIL = "email"
//        private const val COL_PASSWORD = "password"
//        private const val COL_ROLE = "role"
//    }
//
//    override fun onCreate(db: SQLiteDatabase?) {
//        val createTable = """
//            CREATE TABLE $TABLE_USERS (
//                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
//                $COL_EMAIL TEXT UNIQUE,
//                $COL_PASSWORD TEXT,
//                $COL_ROLE TEXT
//            )
//        """.trimIndent()
//        db?.execSQL(createTable)
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
//        onCreate(db)
//    }
//
//    // -------------------- Add User --------------------
//    fun addUser(email: String, password: String, role: String = ""): Boolean {
//        val db = writableDatabase
//        val values = ContentValues().apply {
//            put(COL_EMAIL, email)
//            put(COL_PASSWORD, password)
//            put(COL_ROLE, role)
//        }
//        val result = db.insert(TABLE_USERS, null, values)
//        db.close()
//        return result != -1L
//    }
//
//    fun clearUsers() {
//        try {
//            val db = writableDatabase
//            db.delete(TABLE_USERS, null, null)
//            db.close()
//            Log.d("DatabaseHelper", "All users cleared successfully.")
//        } catch (e: Exception) {
//            Log.e("DatabaseHelper", "Error clearing users: ${e.message}")
//        }
//    }
//
//    // -------------------- Check if user exists --------------------
//    fun checkUser(email: String): Boolean {
//        val db = readableDatabase
//        val cursor = db.query(
//            TABLE_USERS,
//            arrayOf(COL_ID),
//            "$COL_EMAIL = ?",
//            arrayOf(email),
//            null,
//            null,
//            null
//        )
//        val exists = cursor.moveToFirst()
//        cursor.close()
//        db.close()
//        return exists
//    }
//
//    // -------------------- Validate user credentials --------------------
//    fun validateUser(email: String, password: String): Boolean {
//        val db = readableDatabase
//        val cursor = db.query(
//            TABLE_USERS,
//            arrayOf(COL_ID),
//            "$COL_EMAIL = ? AND $COL_PASSWORD = ?",
//            arrayOf(email, password),
//            null,
//            null,
//            null
//        )
//        val valid = cursor.moveToFirst()
//        cursor.close()
//        db.close()
//        return valid
//    }
//
//    // -------------------- Get User Role --------------------
//    fun getUserRole(email: String): String {
//        val db = readableDatabase
//        val cursor = db.query(
//            TABLE_USERS,
//            arrayOf(COL_ROLE),
//            "$COL_EMAIL = ?",
//            arrayOf(email),
//            null,
//            null,
//            null
//        )
//        var role = ""
//        if (cursor.moveToFirst()) {
//            role = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE))
//        }
//        cursor.close()
//        db.close()
//        return role
//    }
//}

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

        private const val COLUMN_FULLNAME = "fullName"  // <-- add this

        private const val COLUMN_PHONE = "phone"

        private const val COLUMN_NIC = "nic"
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

    fun clearUsers() {
        val db = writableDatabase
        db.delete("user", null, null)
        db.close()
    }
    fun addUser(email: String, password: String, role: String = ""): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_ROLE, role)
        }
        val success = db.insert(TABLE_USER, null, values)
        db.close()
        return success != -1L
    }

    fun validateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USER WHERE $COLUMN_EMAIL=? AND $COLUMN_PASSWORD=?",
            arrayOf(email, password)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun checkUser(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USER WHERE $COLUMN_EMAIL=?",
            arrayOf(email)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun getLoggedInUserEmail(): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER, // <-- fix here
            arrayOf(COLUMN_EMAIL),
            "$COLUMN_ROLE IS NOT NULL", // Example: logged-in user has a role
            null, null, null, null
        )

        return if (cursor.moveToFirst()) {
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
            cursor.close()
            email
        } else {
            cursor.close()
            null
        }
    }

    fun getUserFullName(email: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_FULLNAME FROM $TABLE_USER WHERE $COLUMN_EMAIL=?",
            arrayOf(email)
        )
        var fullName = ""
        if (cursor.moveToFirst()) {
            fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME))
        }
        cursor.close()
        db.close()
        return fullName
    }

    fun getUserPhone(email: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_PHONE FROM $TABLE_USER WHERE $COLUMN_EMAIL=?",
            arrayOf(email)
        )
        var phone = ""
        if (cursor.moveToFirst()) {
            phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE))
        }
        cursor.close()
        db.close()
        return phone
    }

    fun getUserPassword(email: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_PASSWORD FROM $TABLE_USER WHERE $COLUMN_EMAIL=?",
            arrayOf(email)
        )
        var password = ""
        if (cursor.moveToFirst()) {
            password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
        }
        cursor.close()
        db.close()
        return password
    }

    fun getUserNIC(email: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_NIC FROM $TABLE_USER WHERE $COLUMN_EMAIL=?",
            arrayOf(email)
        )
        var nic = ""
        if (cursor.moveToFirst()) {
            nic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC))
        }
        cursor.close()
        db.close()
        return nic
    }

    fun getUserRole(email: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_ROLE FROM $TABLE_USER WHERE $COLUMN_EMAIL=?",
            arrayOf(email)
        )
        var role = ""
        if (cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
        }
        cursor.close()
        db.close()
        return role
    }

    fun deleteUser(email: String) {
        val db = writableDatabase
        db.delete(TABLE_USER, "$COLUMN_EMAIL=?", arrayOf(email))
        db.close()
    }
}

package com.example.amply.sqlliteDB


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.amply.model.OwnerProfile

class UserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "user_db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USER = "user"

        private const val COLUMN_NIC = "nic"
        private const val COLUMN_FULLNAME = "fullName"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_ROLE = "role"
        private const val COLUMN_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_NIC TEXT PRIMARY KEY,
                $COLUMN_FULLNAME TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_ROLE TEXT,
                $COLUMN_STATUS TEXT
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    // Insert user
    fun insertUser(user: OwnerProfile): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NIC, user.nic)
            put(COLUMN_FULLNAME, user.fullName)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_PHONE, user.phone)
            put(COLUMN_ROLE, user.role)
            put(COLUMN_STATUS, user.status)
        }
        val success = db.insert(TABLE_USER, null, values)
        db.close()
        return success != -1L
    }

    // Update user
    fun updateUser(user: OwnerProfile): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FULLNAME, user.fullName)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_PHONE, user.phone)
            put(COLUMN_ROLE, user.role)
            put(COLUMN_STATUS, user.status)
        }
        val success = db.update(TABLE_USER, values, "$COLUMN_NIC=?", arrayOf(user.nic))
        db.close()
        return success > 0
    }

    // Get logged-in user (assuming single user stored)
    fun getUser(): OwnerProfile? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USER LIMIT 1", null)
        var user: OwnerProfile? = null
        if (cursor.moveToFirst()) {
            user = OwnerProfile(
                nic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC)),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)),
                status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
            )
        }
        cursor.close()
        db.close()
        return user
    }

    // Delete user (on logout)
    fun deleteUser() {
        val db = writableDatabase
        db.delete(TABLE_USER, null, null)
        db.close()
    }
}

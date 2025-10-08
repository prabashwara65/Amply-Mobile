package com.example.amply.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ReservationDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "amply.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_RESERVATIONS = "Reservations"

        private const val COLUMN_ID = "id"
        private const val COLUMN_RESERVATION_CODE = "reservationCode"
        private const val COLUMN_FULL_NAME = "fullName"
        private const val COLUMN_NIC = "nic"
        private const val COLUMN_VEHICLE_NUMBER = "vehicleNumber"
        private const val COLUMN_STATION_ID = "stationId"
        private const val COLUMN_STATION_NAME = "stationName"
        private const val COLUMN_SLOT_NO = "slotNo"
        private const val COLUMN_BOOKING_DATE = "bookingDate"
        private const val COLUMN_RESERVATION_DATE = "reservationDate"
        private const val COLUMN_START_TIME = "startTime"
        private const val COLUMN_END_TIME = "endTime"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_QR_CODE = "qrCode"
        private const val COLUMN_CREATED_AT = "createdAt"
        private const val COLUMN_UPDATED_AT = "updatedAt"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_RESERVATIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RESERVATION_CODE TEXT UNIQUE,
                $COLUMN_FULL_NAME TEXT,
                $COLUMN_NIC TEXT,
                $COLUMN_VEHICLE_NUMBER TEXT,
                $COLUMN_STATION_ID TEXT,
                $COLUMN_STATION_NAME TEXT,
                $COLUMN_SLOT_NO INTEGER,
                $COLUMN_BOOKING_DATE TEXT,
                $COLUMN_RESERVATION_DATE TEXT,
                $COLUMN_START_TIME TEXT,
                $COLUMN_END_TIME TEXT,
                $COLUMN_STATUS TEXT,
                $COLUMN_QR_CODE TEXT,
                $COLUMN_CREATED_AT TEXT,
                $COLUMN_UPDATED_AT TEXT
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVATIONS")
        onCreate(db)
    }

    /** ✅ Add a new reservation */
    fun addReservation(
        reservationCode: String,
        fullName: String,
        nic: String,
        vehicleNumber: String,
        stationId: String,
        stationName: String,
        slotNo: Int,
        bookingDate: String,
        reservationDate: String,
        startTime: String,
        endTime: String,
        status: String,
        qrCode: String?,
        createdAt: String,
        updatedAt: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RESERVATION_CODE, reservationCode)
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_NIC, nic)
            put(COLUMN_VEHICLE_NUMBER, vehicleNumber)
            put(COLUMN_STATION_ID, stationId)
            put(COLUMN_STATION_NAME, stationName)
            put(COLUMN_SLOT_NO, slotNo)
            put(COLUMN_BOOKING_DATE, bookingDate)
            put(COLUMN_RESERVATION_DATE, reservationDate)
            put(COLUMN_START_TIME, startTime)
            put(COLUMN_END_TIME, endTime)
            put(COLUMN_STATUS, status)
            put(COLUMN_QR_CODE, qrCode)
            put(COLUMN_CREATED_AT, createdAt)
            put(COLUMN_UPDATED_AT, updatedAt)
        }

        val result = db.insert(TABLE_RESERVATIONS, null, values)
        db.close()
        return result != -1L
    }

    /** ✅ Get all reservations */
    fun getAllReservations(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_RESERVATIONS", null)
    }

    /** ✅ Get reservation by reservationCode */
    fun getReservationByCode(reservationCode: String): Cursor? {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_RESERVATIONS WHERE $COLUMN_RESERVATION_CODE = ?",
            arrayOf(reservationCode)
        )
    }

    /** ✅ Update reservation by code */
    fun updateReservation(
        reservationCode: String,
        fullName: String,
        nic: String,
        vehicleNumber: String,
        stationId: String,
        stationName: String,
        slotNo: Int,
        reservationDate: String,
        startTime: String,
        endTime: String,
        status: String,
        updatedAt: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_NIC, nic)
            put(COLUMN_VEHICLE_NUMBER, vehicleNumber)
            put(COLUMN_STATION_ID, stationId)
            put(COLUMN_STATION_NAME, stationName)
            put(COLUMN_SLOT_NO, slotNo)
            put(COLUMN_RESERVATION_DATE, reservationDate)
            put(COLUMN_START_TIME, startTime)
            put(COLUMN_END_TIME, endTime)
            put(COLUMN_STATUS, status)
            put(COLUMN_UPDATED_AT, updatedAt)
        }

        val result = db.update(
            TABLE_RESERVATIONS,
            values,
            "$COLUMN_RESERVATION_CODE = ?",
            arrayOf(reservationCode)
        )
        db.close()
        return result > 0
    }

    /** ✅ Delete reservation by code */
    fun deleteReservation(reservationCode: String): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_RESERVATIONS, "$COLUMN_RESERVATION_CODE = ?", arrayOf(reservationCode))
        db.close()
        return result > 0
    }

    /** ✅ Clear all reservations */
    fun clearReservations() {
        val db = writableDatabase
        try {
            db.execSQL("DELETE FROM $TABLE_RESERVATIONS")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }

}
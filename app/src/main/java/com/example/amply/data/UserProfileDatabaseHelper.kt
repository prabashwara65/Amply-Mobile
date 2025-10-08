package com.example.amply.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.amply.ui.reservation.ChargingStation
import com.example.amply.ui.reservation.Reservation

class UserProfileDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "amply.db"
        private const val DATABASE_VERSION = 2

        // Existing EvOwners table constants
        private const val TABLE_USERS = "EvOwners"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"

        private const val TABLE_RESERVATIONS = "Reservations"
        private const val COLUMN_RES_ID = "id"
        private const val COLUMN_RES_USER_ID = "userId"
        private const val COLUMN_RES_STATION_ID = "stationId"
        private const val COLUMN_RES_STATION_NAME = "stationName"
        private const val COLUMN_RES_DATE = "reservationDate"
        private const val COLUMN_RES_TIME = "reservationTime"
        private const val COLUMN_RES_STATUS = "status" // pending, confirmed, cancelled
        private const val COLUMN_RES_CREATED_AT = "createdAt"

        private const val TABLE_STATIONS = "ChargingStations"
        private const val COLUMN_STATION_ID = "id"
        private const val COLUMN_STATION_NAME = "name"
        private const val COLUMN_STATION_ADDRESS = "address"
        private const val COLUMN_STATION_LAT = "latitude"
        private const val COLUMN_STATION_LNG = "longitude"
        private const val COLUMN_STATION_TYPE = "type" // AC/DC
        private const val COLUMN_STATION_AVAILABLE_SLOTS = "availableSlots"
        private const val COLUMN_STATION_TOTAL_SLOTS = "totalSlots"
        private const val COLUMN_STATION_STATUS = "status" // active/inactive
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Existing EvOwners table creation
        val createTable = ("CREATE TABLE $TABLE_USERS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_USERNAME TEXT UNIQUE, "
                + "$COLUMN_PASSWORD TEXT)")
        db?.execSQL(createTable)

        val createReservationsTable = ("CREATE TABLE $TABLE_RESERVATIONS ("
                + "$COLUMN_RES_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_RES_USER_ID INTEGER, "
                + "$COLUMN_RES_STATION_ID INTEGER, "
                + "$COLUMN_RES_STATION_NAME TEXT, "
                + "$COLUMN_RES_DATE TEXT, "
                + "$COLUMN_RES_TIME TEXT, "
                + "$COLUMN_RES_STATUS TEXT, "
                + "$COLUMN_RES_CREATED_AT TEXT)")
        db?.execSQL(createReservationsTable)

        val createStationsTable = ("CREATE TABLE $TABLE_STATIONS ("
                + "$COLUMN_STATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_STATION_NAME TEXT, "
                + "$COLUMN_STATION_ADDRESS TEXT, "
                + "$COLUMN_STATION_LAT REAL, "
                + "$COLUMN_STATION_LNG REAL, "
                + "$COLUMN_STATION_TYPE TEXT, "
                + "$COLUMN_STATION_AVAILABLE_SLOTS INTEGER, "
                + "$COLUMN_STATION_TOTAL_SLOTS INTEGER, "
                + "$COLUMN_STATION_STATUS TEXT)")
        db?.execSQL(createStationsTable)

        insertSampleStations(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_STATIONS")
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

    private fun insertSampleStations(db: SQLiteDatabase?) {
        val stations = listOf(
            arrayOf("EV Hub Downtown", "123 Main St, Colombo", 6.9271, 79.8612, "AC", 3, 5, "active"),
            arrayOf("Quick Charge Station", "456 Galle Rd, Colombo", 6.9147, 79.8731, "DC", 2, 4, "active"),
            arrayOf("Green Energy Point", "789 Kandy Rd, Colombo", 6.9497, 79.8500, "AC", 4, 6, "active"),
            arrayOf("Power Station Central", "321 Negombo Rd, Colombo", 6.9388, 79.8542, "DC", 1, 3, "active"),
            arrayOf("Eco Charge Hub", "654 Baseline Rd, Colombo", 6.9034, 79.8612, "AC", 5, 8, "active")
        )

        stations.forEach { station ->
            val values = ContentValues().apply {
                put(COLUMN_STATION_NAME, station[0] as String)
                put(COLUMN_STATION_ADDRESS, station[1] as String)
                put(COLUMN_STATION_LAT, station[2] as Double)
                put(COLUMN_STATION_LNG, station[3] as Double)
                put(COLUMN_STATION_TYPE, station[4] as String)
                put(COLUMN_STATION_AVAILABLE_SLOTS, station[5] as Int)
                put(COLUMN_STATION_TOTAL_SLOTS, station[6] as Int)
                put(COLUMN_STATION_STATUS, station[7] as String)
            }
            db?.insert(TABLE_STATIONS, null, values)
        }
    }

    fun addReservation(userId: Int, stationId: Int, stationName: String, date: String, time: String, status: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RES_USER_ID, userId)
            put(COLUMN_RES_STATION_ID, stationId)
            put(COLUMN_RES_STATION_NAME, stationName)
            put(COLUMN_RES_DATE, date)
            put(COLUMN_RES_TIME, time)
            put(COLUMN_RES_STATUS, status)
            put(COLUMN_RES_CREATED_AT, System.currentTimeMillis().toString())
        }
        val result = db.insert(TABLE_RESERVATIONS, null, values)
        db.close()
        return result != -1L
    }

    fun getPendingReservations(userId: Int): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_RESERVATIONS WHERE $COLUMN_RES_USER_ID = ? AND $COLUMN_RES_STATUS = 'pending' ORDER BY $COLUMN_RES_DATE ASC",
            arrayOf(userId.toString())
        )
    }

    fun getConfirmedReservations(userId: Int): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_RESERVATIONS WHERE $COLUMN_RES_USER_ID = ? AND $COLUMN_RES_STATUS = 'confirmed' ORDER BY $COLUMN_RES_DATE ASC",
            arrayOf(userId.toString())
        )
    }

    fun getPendingReservationsCount(userId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_RESERVATIONS WHERE $COLUMN_RES_USER_ID = ? AND $COLUMN_RES_STATUS = 'pending'",
            arrayOf(userId.toString())
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    fun getConfirmedReservationsCount(userId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_RESERVATIONS WHERE $COLUMN_RES_USER_ID = ? AND $COLUMN_RES_STATUS = 'confirmed'",
            arrayOf(userId.toString())
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    fun updateReservationStatus(reservationId: Int, status: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RES_STATUS, status)
        }
        val result = db.update(TABLE_RESERVATIONS, values, "$COLUMN_RES_ID = ?", arrayOf(reservationId.toString()))
        db.close()
        return result > 0
    }

    fun deleteReservation(reservationId: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_RESERVATIONS, "$COLUMN_RES_ID = ?", arrayOf(reservationId.toString()))
        db.close()
        return result > 0
    }

    fun getAllChargingStations(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_STATIONS WHERE $COLUMN_STATION_STATUS = 'active'",
            null
        )
    }

    fun getChargingStationById(stationId: Int): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_STATIONS WHERE $COLUMN_STATION_ID = ?",
            arrayOf(stationId.toString())
        )
    }

    fun getReservationsByStatus(userId: Int, status: String): List<Reservation> {
        val reservations = mutableListOf<Reservation>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_RESERVATIONS WHERE $COLUMN_RES_USER_ID = ? AND $COLUMN_RES_STATUS = ? ORDER BY $COLUMN_RES_DATE ASC",
            arrayOf(userId.toString(), status)
        )

        if (cursor.moveToFirst()) {
            do {
                val reservation = Reservation(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RES_ID)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RES_USER_ID)),
                    stationId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RES_STATION_ID)),
                    stationName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_STATION_NAME)),
                    reservationDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_DATE)),
                    reservationTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_TIME)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_STATUS)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_CREATED_AT))
                )
                reservations.add(reservation)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return reservations
    }

    fun getAllChargingStationsList(): List<ChargingStation> {
        val stations = mutableListOf<ChargingStation>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_STATIONS WHERE $COLUMN_STATION_STATUS = 'active'",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val station = ChargingStation(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATION_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATION_NAME)),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATION_ADDRESS)),
                    latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_STATION_LAT)),
                    longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_STATION_LNG)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATION_TYPE)),
                    availableSlots = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATION_AVAILABLE_SLOTS)),
                    totalSlots = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATION_TOTAL_SLOTS)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATION_STATUS))
                )
                stations.add(station)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return stations
    }
}
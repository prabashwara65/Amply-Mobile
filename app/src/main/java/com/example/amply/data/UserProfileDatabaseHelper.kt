package com.example.amply.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.amply.model.ChargingStation
import com.example.amply.model.Reservation

class UserProfileDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "amply.db"
        private const val DATABASE_VERSION = 3 // incremented version since schema changed

        // EvOwners table
        private const val TABLE_USERS = "EvOwners"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"

        // Reservations table
        private const val TABLE_RESERVATIONS = "Reservations"
        private const val COLUMN_RES_ID = "id"
        private const val COLUMN_RES_USER_ID = "userId"
        private const val COLUMN_RES_STATION_ID = "stationId"
        private const val COLUMN_RES_STATION_NAME = "stationName"
        private const val COLUMN_RES_DATE = "reservationDate"
        private const val COLUMN_RES_START_TIME = "startTime"
        private const val COLUMN_RES_END_TIME = "endTime"
        private const val COLUMN_RES_STATUS = "status" // pending, confirmed, cancelled
        private const val COLUMN_RES_CREATED_AT = "createdAt"

        // ChargingStations table
        private const val TABLE_STATIONS = "ChargingStations"
        private const val COLUMN_STATION_ID = "id"
        private const val COLUMN_STATION_NAME = "name"
        private const val COLUMN_STATION_ADDRESS = "address"
        private const val COLUMN_STATION_LAT = "latitude"
        private const val COLUMN_STATION_LNG = "longitude"
        private const val COLUMN_STATION_TYPE = "type"
        private const val COLUMN_STATION_AVAILABLE_SLOTS = "availableSlots"
        private const val COLUMN_STATION_TOTAL_SLOTS = "totalSlots"
        private const val COLUMN_STATION_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create EvOwners table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT
            )
        """.trimIndent()
        db?.execSQL(createUsersTable)

        // Create Reservations table (updated: startTime, endTime instead of reservationTime)
        val createReservationsTable = """
            CREATE TABLE $TABLE_RESERVATIONS (
                $COLUMN_RES_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RES_USER_ID INTEGER,
                $COLUMN_RES_STATION_ID INTEGER,
                $COLUMN_RES_STATION_NAME TEXT,
                $COLUMN_RES_DATE TEXT,
                $COLUMN_RES_START_TIME TEXT,
                $COLUMN_RES_END_TIME TEXT,
                $COLUMN_RES_STATUS TEXT,
                $COLUMN_RES_CREATED_AT TEXT
            )
        """.trimIndent()
        db?.execSQL(createReservationsTable)

        // Create ChargingStations table
        val createStationsTable = """
            CREATE TABLE $TABLE_STATIONS (
                $COLUMN_STATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_STATION_NAME TEXT,
                $COLUMN_STATION_ADDRESS TEXT,
                $COLUMN_STATION_LAT REAL,
                $COLUMN_STATION_LNG REAL,
                $COLUMN_STATION_TYPE TEXT,
                $COLUMN_STATION_AVAILABLE_SLOTS INTEGER,
                $COLUMN_STATION_TOTAL_SLOTS INTEGER,
                $COLUMN_STATION_STATUS TEXT
            )
        """.trimIndent()
        db?.execSQL(createStationsTable)

        insertSampleStations(db)
    }

    // Called when database version is upgraded. Drops old tables and recreates new ones.
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_STATIONS")
        onCreate(db)
    }

    // ---------------- USERS ----------------

    // Adds a new EV owner (username and password) to the database.
    fun addUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    // Checks if a user already exists by username.
    fun checkUser(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?", arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Validates user credentials for login.
    fun validateUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password)
        )
        val valid = cursor.count > 0
        cursor.close()
        db.close()
        return valid
    }

    // ---------------- STATIONS ----------------

    //Inserts sample charging stations into the database for initial testing.
    private fun insertSampleStations(db: SQLiteDatabase?) {
        val stations = listOf(
            arrayOf("EV Hub Downtown", "123 Main St, Colombo", 6.9271, 79.8612, "AC", 3, 5, "active"),
            arrayOf("Quick Charge Station", "456 Galle Rd, Colombo", 6.9147, 79.8731, "DC", 2, 4, "active"),
            arrayOf("Green Energy Point", "789 Kandy Rd, Colombo", 6.9497, 79.8500, "AC", 4, 6, "active"),
            arrayOf("Power Station Central", "321 Negombo Rd, Colombo", 6.9388, 79.8542, "DC", 1, 3, "active"),
            arrayOf("Eco Charge Hub", "654 Baseline Rd, Colombo", 6.9034, 79.8612, "AC", 5, 8, "active")
        )

        stations.forEach {
            val values = ContentValues().apply {
                put(COLUMN_STATION_NAME, it[0] as String)
                put(COLUMN_STATION_ADDRESS, it[1] as String)
                put(COLUMN_STATION_LAT, it[2] as Double)
                put(COLUMN_STATION_LNG, it[3] as Double)
                put(COLUMN_STATION_TYPE, it[4] as String)
                put(COLUMN_STATION_AVAILABLE_SLOTS, it[5] as Int)
                put(COLUMN_STATION_TOTAL_SLOTS, it[6] as Int)
                put(COLUMN_STATION_STATUS, it[7] as String)
            }
            db?.insert(TABLE_STATIONS, null, values)
        }
    }

    // Retrieves all active charging stations as a list of ChargingStation objects.
    fun getAllChargingStationsList(): List<ChargingStation> {
        val stations = mutableListOf<ChargingStation>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_STATIONS WHERE $COLUMN_STATION_STATUS = 'active'", null)
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

    // ---------------- RESERVATIONS ----------------

    // Inserts a new reservation record for an EV owner.
    fun addReservation(
        userId: Int,
        stationId: Int,
        stationName: String,
        date: String,
        startTime: String,
        endTime: String,
        status: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RES_USER_ID, userId)
            put(COLUMN_RES_STATION_ID, stationId)
            put(COLUMN_RES_STATION_NAME, stationName)
            put(COLUMN_RES_DATE, date)
            put(COLUMN_RES_START_TIME, startTime)
            put(COLUMN_RES_END_TIME, endTime)
            put(COLUMN_RES_STATUS, status)
            put(COLUMN_RES_CREATED_AT, System.currentTimeMillis().toString())
        }
        val result = db.insert(TABLE_RESERVATIONS, null, values)
        db.close()
        return result != -1L
    }

    // Retrieves reservations filtered by userId and status
    fun getReservationsByStatus(userId: Int, status: String): List<Reservation> {
        val reservations = mutableListOf<Reservation>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_RESERVATIONS WHERE $COLUMN_RES_USER_ID = ? AND $COLUMN_RES_STATUS = ? ORDER BY $COLUMN_RES_DATE ASC",
            arrayOf(userId.toString(), status)
        )

        if (cursor.moveToFirst()) {
            do {
                val reservation = Reservation(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_ID)),
                    reservationCode = "N/A",
                    fullName = "Unknown",
                    nic = null,
                    vehicleNumber = "N/A",
                    stationId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_STATION_ID)),
                    stationName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_STATION_NAME)),
                    slotNo = 0,
                    reservationDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_DATE)),
                    startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_START_TIME)),
                    endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_END_TIME)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_STATUS)),
                    qrCode = null
                )
                reservations.add(reservation)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return reservations
    }

    // Updates the status of a reservation
    fun updateReservationStatus(reservationId: Int, status: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put(COLUMN_RES_STATUS, status) }
        val result = db.update(TABLE_RESERVATIONS, values, "$COLUMN_RES_ID = ?", arrayOf(reservationId.toString()))
        db.close()
        return result > 0
    }

    // Deletes a reservation record by its ID.
    fun deleteReservation(reservationId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_RESERVATIONS, "$COLUMN_RES_ID = ?", arrayOf(reservationId.toString()))
        db.close()
        return result > 0
    }
}
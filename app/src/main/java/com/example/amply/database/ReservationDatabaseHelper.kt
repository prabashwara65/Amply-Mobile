package com.example.amply.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.amply.model.Reservation

class ReservationDatabaseHelper (context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        companion object{
            private const val DATABASE_NAME = "amply.db"
            private const val DATABASE_VERSION = 1
            private const val TABLE_RESERVATIONS = "Reservations"

            private const val COLUMN_ID = "id"
            private const val COLUMN_RES_CODE = "reservationCode"
            private const val COLUMN_FULLNAME = "fullName"
            private const val COLUMN_NIC = "nic"
            private const val COLUMN_VEHICLE = "vehicleNumber"
            private const val COLUMN_STATION_ID = "stationId"
            private const val COLUMN_STATION_NAME = "stationName"
            private const val COLUMN_SLOT = "slotNo"
            private const val COLUMN_RES_DATE = "reservationDate"
            private const val COLUMN_START_TIME = "startTime"
            private const val COLUMN_END_TIME = "endTime"
            private const val COLUMN_STATUS = "status"
            private const val COLUMN_QR = "qrCode"
        }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_RESERVATIONS (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_RES_CODE TEXT UNIQUE,
            $COLUMN_FULLNAME TEXT,
            $COLUMN_NIC TEXT,
            $COLUMN_VEHICLE TEXT,
            $COLUMN_STATION_ID TEXT,
            $COLUMN_STATION_NAME TEXT,
            $COLUMN_SLOT INTEGER,
            $COLUMN_RES_DATE TEXT,
            $COLUMN_START_TIME TEXT,
            $COLUMN_END_TIME TEXT,
            $COLUMN_STATUS TEXT,
            $COLUMN_QR TEXT
            )
            """.trimIndent()
         db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
       db?.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVATIONS")
        onCreate(db)
    }

    //Insert new reservation
    fun addReservation(reservation: Reservation) : Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RES_CODE, reservation.reservationCode)
            put(COLUMN_FULLNAME, reservation.fullName)
            put(COLUMN_NIC, reservation.nic)
            put(COLUMN_VEHICLE, reservation.vehicleNumber)
            put(COLUMN_STATION_ID, reservation.stationId)
            put(COLUMN_STATION_NAME, reservation.stationName)
            put(COLUMN_SLOT, reservation.slotNo)
            put(COLUMN_RES_DATE, reservation.reservationDate)
            put(COLUMN_START_TIME, reservation.startTime)
            put(COLUMN_END_TIME, reservation.endTime)
            put(COLUMN_STATUS, reservation.status)
            put(COLUMN_QR, reservation.qrCode)
        }
        val result = db.insert(TABLE_RESERVATIONS, null, values)
        db.close()
        return result != -1L

    }

    //get all reservations
    fun getAllReservations() : List<Reservation> {
        val reservations = mutableListOf<Reservation>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_RESERVATIONS", null)
        if(cursor.moveToFirst()){
            do {
                reservations.add(
                    Reservation(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)).toString(),
                    reservationCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_CODE)),
                    fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME)),
                    nic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC)),
                    vehicleNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE)),
                    stationId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATION_ID)),
                    stationName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATION_NAME)),
                    slotNo = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SLOT)),
                    reservationDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RES_DATE)),
                    startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)),
                    endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                    qrCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QR))
                )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return reservations
    }

    //update reservation
    fun updateReservationDetails(reservation: Reservation) : Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FULLNAME, reservation.fullName)
            put(COLUMN_NIC, reservation.nic)
            put(COLUMN_VEHICLE, reservation.vehicleNumber)
            put(COLUMN_STATION_ID, reservation.stationId)
            put(COLUMN_STATION_NAME, reservation.stationName)
            put(COLUMN_SLOT, reservation.slotNo)
            put(COLUMN_RES_DATE, reservation.reservationDate)
            put(COLUMN_START_TIME, reservation.startTime)
            put(COLUMN_END_TIME, reservation.endTime)
            put(COLUMN_STATUS, reservation.status)
            put(COLUMN_QR, reservation.qrCode)
        }
        val result = db.update(
            TABLE_RESERVATIONS,
            values,
            "$COLUMN_RES_CODE = ?",
            arrayOf(reservation.reservationCode)
            )
        db.close()
        return result > 0
    }

    //delete reservation
    fun deleteReservation(reservationCode: String) : Boolean{
        val db = writableDatabase
        val result = db.delete (TABLE_RESERVATIONS ,"$COLUMN_RES_CODE = ?", arrayOf(reservationCode) )
        db.close()
        return  result > 0
    }
}
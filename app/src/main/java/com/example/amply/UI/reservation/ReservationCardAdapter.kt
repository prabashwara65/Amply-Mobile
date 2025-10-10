package com.example.amply.ui.reservation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.amply.R
import com.example.amply.model.ReservationExtended

class ReservationCardAdapter(
    private var reservations: MutableList<ReservationExtended>,
    private val onItemClick: (ReservationExtended) -> Unit
) : RecyclerView.Adapter<ReservationCardAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stationName: TextView = itemView.findViewById(R.id.tvStationName)
        val statusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)
        val reservationCode: TextView = itemView.findViewById(R.id.tvReservationCode)
        val reservationDate: TextView = itemView.findViewById(R.id.tvReservationDate)
        val reservationTime: TextView = itemView.findViewById(R.id.tvReservationTime)
        val slotNumber: TextView = itemView.findViewById(R.id.tvSlotNumber)
        val vehicleNumber: TextView = itemView.findViewById(R.id.tvVehicleNumber)
        val qrCodeContainer: LinearLayout = itemView.findViewById(R.id.qrCodeContainer)
        val qrCodeImage: ImageView = itemView.findViewById(R.id.ivQrCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = reservations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = reservations[position]
        
        // Bind data to views
        holder.stationName.text = reservation.stationName
        holder.reservationCode.text = reservation.reservationCode
        holder.reservationDate.text = reservation.reservationDate
        holder.reservationTime.text = "${reservation.startTime} - ${reservation.endTime}"
        holder.slotNumber.text = "Slot ${reservation.slotNo}"
        holder.vehicleNumber.text = reservation.vehicleNumber
        
        // Set status badge
        holder.statusBadge.text = reservation.status.uppercase()
        
        // Set status badge background based on status
        val statusBackground = when (reservation.status.lowercase()) {
            "pending" -> R.drawable.status_badge_pending
            "confirmed" -> R.drawable.status_badge_confirmed
            "done", "completed" -> R.drawable.status_badge_done
            else -> R.drawable.status_badge_pending
        }
        holder.statusBadge.setBackgroundResource(statusBackground)

        if (reservation.status.lowercase() == "confirmed" && !reservation.qrCode.isNullOrEmpty()) {
            holder.qrCodeContainer.visibility = View.VISIBLE
            
            // Decode and display QR code
            val qrBitmap = decodeBase64ToBitmap(reservation.qrCode)
            if (qrBitmap != null) {
                holder.qrCodeImage.setImageBitmap(qrBitmap)
                
                // Add click listener to show enlarged QR code
                holder.qrCodeContainer.setOnClickListener {
                    showEnlargedQrCode(holder.itemView.context, reservation, qrBitmap)
                }
            } else {
                holder.qrCodeContainer.visibility = View.GONE
            }
        } else {
            holder.qrCodeContainer.visibility = View.GONE
        }
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(reservation)
        }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            // Remove data URI prefix if present (e.g., "data:image/png;base64,")
            val cleanBase64 = base64String.substringAfter("base64,").trim()
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showEnlargedQrCode(context: android.content.Context, reservation: ReservationExtended, qrBitmap: Bitmap) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_qr_code, null)
        val dialog = AlertDialog.Builder(context, R.style.TransparentDialog)
            .setView(dialogView)
            .create()

        val ivDialogQrCode = dialogView.findViewById<ImageView>(R.id.ivDialogQrCode)
        val tvDialogReservationCode = dialogView.findViewById<TextView>(R.id.tvDialogReservationCode)
        val btnClose = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClose)

        ivDialogQrCode.setImageBitmap(qrBitmap)
        tvDialogReservationCode.text = reservation.reservationCode

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun updateData(newData: List<ReservationExtended>) {
        reservations.clear()
        reservations.addAll(newData)
        notifyDataSetChanged()
    }
}

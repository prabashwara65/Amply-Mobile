package com.example.amply.ui.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.amply.R
import com.example.amply.model.ReservationExtended

class ReservationViewAdapter(
    private var reservations: MutableList<ReservationExtended>,
    private val onItemClick: (ReservationExtended) -> Unit
) : RecyclerView.Adapter<ReservationViewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reservationId: TextView = itemView.findViewById(R.id.tvReservationId)
        val stationName: TextView = itemView.findViewById(R.id.stationName)
        val reservationDate: TextView = itemView.findViewById(R.id.reservationDate)
        val reservationTime: TextView = itemView.findViewById(R.id.reservationTime)
        val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = reservations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.reservationId.text = "ID: ${reservation.reservationCode}"
        holder.stationName.text = reservation.stationName
        holder.reservationDate.text = reservation.reservationDate
        holder.reservationTime.text = reservation.startTime
        holder.statusBadge.text = reservation.status.uppercase()
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(reservation)
        }
    }

    fun updateData(newData: List<ReservationExtended>) {
        reservations.clear()
        reservations.addAll(newData)
        notifyDataSetChanged()
    }
}

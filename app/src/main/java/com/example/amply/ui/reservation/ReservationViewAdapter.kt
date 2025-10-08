package com.example.amply.ui.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.amply.R

/**
 * Adapter for displaying reservations in RecyclerView
 */
class ReservationViewAdapter(
    private val reservations: MutableList<ReservationListActivity.Reservation>,
    private val onItemClick: (ReservationListActivity.Reservation) -> Unit
) : RecyclerView.Adapter<ReservationViewAdapter.ReservationViewHolder>() {

    class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stationName: TextView = itemView.findViewById(R.id.stationName)
        val reservationDate: TextView = itemView.findViewById(R.id.reservationDate)
        val reservationTime: TextView = itemView.findViewById(R.id.reservationTime)
        val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.stationName.text = reservation.stationName
        holder.reservationDate.text = reservation.reservationDate
        holder.reservationTime.text = "${reservation.startTime} - ${reservation.endTime}"
        holder.statusBadge.text = reservation.status.uppercase()

        holder.itemView.setOnClickListener {
            onItemClick(reservation)
        }
    }

    override fun getItemCount(): Int = reservations.size

    // Method to update adapter data
    fun updateData(newReservations: List<ReservationListActivity.Reservation>) {
        reservations.clear()
        reservations.addAll(newReservations)
        notifyDataSetChanged()
    }
}

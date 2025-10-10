package com.example.amply.ui.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.amply.R

class ReservationViewAdapter(
    private var reservations: MutableList<ReservationListActivity.ReservationExtended>,
    private val onItemClick: (ReservationListActivity.ReservationExtended) -> Unit,

    //update
    private val onUpdateClick : (ReservationListActivity.ReservationExtended) -> Unit,
    //Delete
    private val onDeleteClick: (ReservationListActivity.ReservationExtended) -> Unit
) : RecyclerView.Adapter<ReservationViewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reservationId: TextView = itemView.findViewById(R.id.tvReservationId)
        val stationName: TextView = itemView.findViewById(R.id.stationName)
        val reservationDate: TextView = itemView.findViewById(R.id.reservationDate)
        val reservationTime: TextView = itemView.findViewById(R.id.reservationTime)
        val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)
        val btnUpdate: Button = itemView.findViewById(R.id.btnUpdate)
        val btnDelete : Button = itemView.findViewById(R.id.btnDelete)


    }

    // Inflates the layout for a single item in RecyclerView and creates a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ViewHolder(view)
    }

    // Returns the number of reservation items in the list
    override fun getItemCount() = reservations.size

    // Binds data to the ViewHolder for the given position
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

        holder.btnUpdate.setOnClickListener { onUpdateClick(reservation) }
        holder.btnDelete.setOnClickListener { onDeleteClick(reservation) }
    }

    // Updates the adapter's data with a new list of reservations and refreshes RecyclerView
    fun updateData(newData: List<ReservationListActivity.ReservationExtended>) {
        reservations.clear()
        reservations.addAll(newData)
        notifyDataSetChanged()
    }
}
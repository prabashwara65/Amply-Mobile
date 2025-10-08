package com.example.amply

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.amply.R

class ReservationAdapter(private var reservations: MutableList<ReservationListActivity.Reservation>) :
    RecyclerView.Adapter<ReservationAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val code: TextView = itemView.findViewById(R.id.tvCode)
        val name: TextView = itemView.findViewById(R.id.tvName)
        val station: TextView = itemView.findViewById(R.id.tvStation)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_reservation, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = reservations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.code.text = reservation.reservationCode
        holder.name.text = reservation.fullName
        holder.station.text = reservation.stationName
        holder.status.text = reservation.status
    }

    fun updateData(newData: List<ReservationListActivity.Reservation>) {
        reservations.clear()
        reservations.addAll(newData)
        notifyDataSetChanged()
    }
}

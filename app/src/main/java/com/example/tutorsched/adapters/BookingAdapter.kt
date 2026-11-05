package com.example.tutorsched

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookingAdapter(private val context: Context, private val bookingList: List<Booking>) :
    RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTutor: TextView = itemView.findViewById(R.id.tvBookingTutor)
        val tvSubject: TextView = itemView.findViewById(R.id.tvBookingSubject)
        val tvTime: TextView = itemView.findViewById(R.id.tvBookingTime)
        val tvStatus: TextView = itemView.findViewById(R.id.tvBookingStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]
        holder.tvTutor.text = "Tutor: ${booking.tutorName}"
        holder.tvSubject.text = "Subject: ${booking.subject}"
        holder.tvTime.text = "${booking.startTime} - ${booking.endTime} (${booking.date})"
        holder.tvStatus.text = "Status: ${booking.status}"
    }

    override fun getItemCount(): Int = bookingList.size
}
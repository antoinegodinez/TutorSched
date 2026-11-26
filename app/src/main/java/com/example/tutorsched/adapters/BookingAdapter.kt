package com.example.tutorsched.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorsched.R
import com.example.tutorsched.models.Booking

class BookingAdapter(private val bookingList: List<Booking>) :
    RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTutor: TextView = itemView.findViewById(R.id.tvBookingTutor)
        val tvSubject: TextView = itemView.findViewById(R.id.tvBookingSubject)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvBookingDateTime)
        val tvStatus: TextView = itemView.findViewById(R.id.tvBookingStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]
        holder.tvTutor.text = "Tutor: ${booking.tutorName}"
        holder.tvSubject.text = "Subject: ${booking.subject}"
        holder.tvDateTime.text = "Date: ${booking.date} | ${booking.startTime} - ${booking.endTime}"
        holder.tvStatus.text = "Status: ${booking.status}"
    }

    override fun getItemCount(): Int = bookingList.size
}
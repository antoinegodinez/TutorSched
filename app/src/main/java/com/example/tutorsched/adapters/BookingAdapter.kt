package com.example.tutorsched.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorsched.R
import com.example.tutorsched.models.Booking

class BookingAdapter(
    private val bookingList: List<Booking>,
    private val showTutorActions: Boolean = false,
    private val onStatusAction: ((Booking, String) -> Unit)? = null,
    private val showLearnerActions: Boolean = false,
    private val onCancelBooking: ((Booking) -> Unit)? = null,
    private val onDeleteBooking: ((Booking) -> Unit)? = null
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTutor: TextView = itemView.findViewById(R.id.tvBookingTutor)
        val tvSubject: TextView = itemView.findViewById(R.id.tvBookingSubject)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvBookingDateTime)
        val tvStatus: TextView = itemView.findViewById(R.id.tvBookingStatus)
        val llTutorActions: LinearLayout = itemView.findViewById(R.id.llTutorActions)
        val btnApprove: Button = itemView.findViewById(R.id.btnApproveBooking)
        val btnDecline: Button = itemView.findViewById(R.id.btnDeclineBooking)
        val btnComplete: Button = itemView.findViewById(R.id.btnCompleteBooking)
        val llLearnerActions: LinearLayout = itemView.findViewById(R.id.llLearnerActions)
        val btnCancel: Button = itemView.findViewById(R.id.btnCancelBooking)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeleteBooking)
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
        holder.tvDateTime.text = if (booking.endTime.isNotEmpty()) {
            "Date: ${booking.date} | ${booking.startTime} - ${booking.endTime}"
        } else {
            "Date: ${booking.date} | ${booking.startTime}"
        }
        holder.tvStatus.text = "Status: ${booking.status}"

        val hasDocId = booking.documentId.isNotEmpty()
        val isPending = booking.status.equals("Pending", ignoreCase = true)
        val isConfirmed = booking.status.equals("Confirmed", ignoreCase = true)
        val isCompleted = booking.status.equals("Completed", ignoreCase = true)
        val isAccepted = isConfirmed || booking.status.equals("Accepted", ignoreCase = true)
        val isClosed = isCompleted ||
                booking.status.equals("Cancelled", ignoreCase = true) ||
                booking.status.equals("Declined", ignoreCase = true) ||
                booking.status.equals("Rejected", ignoreCase = true)

        val showApproveDecline = showTutorActions && hasDocId && isPending
        val showComplete = showTutorActions && hasDocId && isConfirmed

        holder.btnApprove.visibility = if (showApproveDecline) View.VISIBLE else View.GONE
        holder.btnDecline.visibility = if (showApproveDecline) View.VISIBLE else View.GONE
        holder.btnComplete.visibility = if (showComplete) View.VISIBLE else View.GONE
        holder.llTutorActions.visibility = if (showApproveDecline || showComplete) View.VISIBLE else View.GONE

        holder.btnApprove.setOnClickListener {
            onStatusAction?.invoke(booking, "Confirmed")
        }
        holder.btnDecline.setOnClickListener {
            onStatusAction?.invoke(booking, "Declined")
        }
        holder.btnComplete.setOnClickListener {
            onStatusAction?.invoke(booking, "Completed")
        }

        val canCancel = showLearnerActions && hasDocId && !isAccepted && !isClosed
        val canDelete = showLearnerActions && hasDocId && !isAccepted && !isCompleted

        holder.btnCancel.visibility = if (canCancel) View.VISIBLE else View.GONE
        holder.btnDelete.visibility = if (canDelete) View.VISIBLE else View.GONE
        holder.llLearnerActions.visibility = if (canCancel || canDelete) View.VISIBLE else View.GONE

        holder.btnCancel.setOnClickListener {
            onCancelBooking?.invoke(booking)
        }
        holder.btnDelete.setOnClickListener {
            onDeleteBooking?.invoke(booking)
        }
    }

    override fun getItemCount(): Int = bookingList.size
}
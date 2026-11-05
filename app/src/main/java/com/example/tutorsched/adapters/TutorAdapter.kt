package com.example.tutorsched.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorsched.R
import com.example.tutorsched.models.Tutor

class TutorAdapter(
    private val tutorList: List<Tutor>,
    private val onBookClick: (Tutor) -> Unit
) : RecyclerView.Adapter<TutorAdapter.TutorViewHolder>() {

    class TutorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.textTutorName)
        val subjectText: TextView = itemView.findViewById(R.id.textTutorSubject)
        val bookButton: Button = itemView.findViewById(R.id.btnViewBook)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tutor, parent, false)
        return TutorViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutorViewHolder, position: Int) {
        val tutor = tutorList[position]
        holder.nameText.text = "Tutor: ${tutor.name}"
        holder.subjectText.text = "Subjects: ${tutor.subject}"

        holder.bookButton.setOnClickListener {
            onBookClick(tutor)
        }
    }

    override fun getItemCount(): Int = tutorList.size
}
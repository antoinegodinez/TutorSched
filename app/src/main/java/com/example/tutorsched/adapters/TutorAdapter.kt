package com.example.tutorsched

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TutorAdapter(private val context: Context, private val tutorList: List<Tutor>) :
    RecyclerView.Adapter<TutorAdapter.TutorViewHolder>() {

    class TutorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvTutorName)
        val tvSubjects: TextView = itemView.findViewById(R.id.tvTutorSubjects)
        val btnBook: Button = itemView.findViewById(R.id.btnBookTutor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_tutor, parent, false)
        return TutorViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutorViewHolder, position: Int) {
        val tutor = tutorList[position]
        holder.tvName.text = tutor.name
        holder.tvSubjects.text = tutor.subjects

        // pass tutor name to booking activity when clicked
        holder.btnBook.setOnClickListener {
            val intent = Intent(context, BookingActivity::class.java).apply {
                putExtra("EXTRA_TUTOR_NAME", tutor.name)
                putExtra("EXTRA_TUTOR_SUBJECTS", tutor.subjects)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = tutorList.size
}
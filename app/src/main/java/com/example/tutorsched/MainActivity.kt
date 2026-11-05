package com.example.tutorsched

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tutorAdapter: TutorAdapter
    private lateinit var tutorList: List<Tutor>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewTutors)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // sample list data to test the UI recycler view
        tutorList = listOf(
            Tutor("Alex Smith", "Programming, CS101", "Available"),
            Tutor("Jamie Reyes", "Database, SQL", "Available"),
            Tutor("Chris Morales", "Data Structures", "Available")
        )

        tutorAdapter = TutorAdapter(this, tutorList)
        recyclerView.adapter = tutorAdapter
    }
}
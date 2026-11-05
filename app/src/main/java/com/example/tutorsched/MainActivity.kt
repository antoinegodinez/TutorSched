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

        // Initialize the RecyclerView from activity_main.xml
        recyclerView = findViewById(R.id.recyclerViewTutors)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Populate local sample mock data matching project requirements
        tutorList = listOf(
            Tutor("Alex", "Programming, CS101", "Available"),
            Tutor("Jamie", "Database, SQL", "Available")
        )

        // Bind the adapter to the RecyclerView
        tutorAdapter = TutorAdapter(this, tutorList)
        recyclerView.adapter = tutorAdapter
    }
}
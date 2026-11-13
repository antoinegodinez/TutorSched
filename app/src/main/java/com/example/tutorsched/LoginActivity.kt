package com.example.tutorsched

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        val btnLoginLearner = findViewById<Button>(R.id.btnLoginLearner)
        val btnLoginTutor = findViewById<Button>(R.id.btnLoginTutor)
        val btnLoginCoordinator = findViewById<Button>(R.id.btnLoginCoordinator)

        // 1. Learner Login Routing
        btnLoginLearner.setOnClickListener {
            if (validateInput(etEmail, etPassword)) {
                Toast.makeText(this, "Logged in as Learner", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // 2. Tutor Login Routing
        btnLoginTutor.setOnClickListener {
            if (validateInput(etEmail, etPassword)) {
                Toast.makeText(this, "Logged in as Tutor", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, TutorScheduleActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // 3. Coordinator Login Routing
        btnLoginCoordinator.setOnClickListener {
            if (validateInput(etEmail, etPassword)) {
                Toast.makeText(this, "Logged in as Coordinator", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, CoordinatorDashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    // Helper validation to ensure text fields aren't completely empty
    private fun validateInput(etEmail: EditText, etPassword: EditText): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
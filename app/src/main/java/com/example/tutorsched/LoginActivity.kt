package com.example.tutorsched

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "LoginActivityDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        val btnLoginLearner = findViewById<Button>(R.id.btnLoginLearner)
        val btnLoginTutor = findViewById<Button>(R.id.btnLoginTutor)
        val btnLoginCoordinator = findViewById<Button>(R.id.btnLoginCoordinator)

        // 1. Learner Login Routing
        btnLoginLearner.setOnClickListener {
            handleFirebaseLogin(etEmail, etPassword, MainActivity::class.java, "Learner")
        }

        // 2. Tutor Login Routing
        btnLoginTutor.setOnClickListener {
            handleFirebaseLogin(etEmail, etPassword, TutorScheduleActivity::class.java, "Tutor")
        }

        // 3. Coordinator Login Routing
        btnLoginCoordinator.setOnClickListener {
            handleFirebaseLogin(etEmail, etPassword, CoordinatorDashboardActivity::class.java, "Coordinator")
        }
    }

    private fun handleFirebaseLogin(etEmail: EditText, etPassword: EditText, destinationClass: Class<*>, roleName: String) {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Attempting Firebase authentication for: $email")

        // Step 1: Always try signing in first to prevent "account already in use" errors
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Sign-in successful for $roleName")
                    Toast.makeText(this, "Welcome back, $roleName!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, destinationClass))
                    finish()
                } else {
                    Log.d(TAG, "Sign-in failed, attempting automatic account creation...", task.exception)

                    // Step 2: If sign-in fails, create a new account for quick testing if it doesn't exist yet
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { createTask ->
                            if (createTask.isSuccessful) {
                                Log.d(TAG, "Account created successfully for $roleName")
                                Toast.makeText(this, "Account created & logged in as $roleName!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, destinationClass))
                                finish()
                            } else {
                                val errorMsg = createTask.exception?.message ?: "Unknown error"
                                Log.e(TAG, "Authentication/Creation failed: $errorMsg", createTask.exception)
                                Toast.makeText(this, "Login Failed: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
    }
}
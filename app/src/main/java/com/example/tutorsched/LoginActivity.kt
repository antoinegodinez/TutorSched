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

    private fun handleFirebaseLogin(
        etEmail: EditText,
        etPassword: EditText,
        destinationClass: Class<*>,
        roleName: String
    ) {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Attempting sign-in for: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Sign-in successful for $roleName")
                    Toast.makeText(this, "Welcome back, $roleName!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, destinationClass))
                    finish()
                } else {
                    val signInError = task.exception?.localizedMessage ?: "Sign-in failed"
                    Log.w(TAG, "Sign-in failed: $signInError", task.exception)

                    // Only try creating a user if the email is not already registered
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { createTask ->
                            if (createTask.isSuccessful) {
                                Log.d(TAG, "Account created for $roleName")
                                Toast.makeText(
                                    this,
                                    "Account created & logged in as $roleName!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this, destinationClass))
                                finish()
                            } else {
                                val message = if (
                                    createTask.exception?.message?.contains("already in use", ignoreCase = true) == true
                                ) {
                                    "Account already exists—check your password ($signInError)"
                                } else {
                                    createTask.exception?.localizedMessage ?: signInError
                                }
                                Log.e(TAG, "Auth error: $message", createTask.exception)
                                Toast.makeText(this, "Login Failed: $message", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
    }
}
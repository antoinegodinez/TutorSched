package com.example.tutorsched

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val selectedRole = getSelectedRole(rgRole)

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSignIn.isEnabled = false
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    btnSignIn.isEnabled = true
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        val typedName = etFullName.text.toString().trim()
                        if (uid != null && typedName.isNotEmpty()) {
                            db.collection("users").document(uid)
                                .set(
                                    mapOf(
                                        "fullName" to typedName,
                                        "email" to email,
                                        "role" to selectedRole
                                    ),
                                    com.google.firebase.firestore.SetOptions.merge()
                                )
                        }
                        openDashboardForRole(selectedRole, typedName.ifEmpty { email.substringBefore("@") })
                    } else {
                        Toast.makeText(
                            this,
                            "Sign-in failed: ${task.exception?.localizedMessage ?: "Check credentials"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val selectedRole = getSelectedRole(rgRole)

            if (fullName.isEmpty() || email.isEmpty() || password.length < 6) {
                Toast.makeText(
                    this,
                    "Enter your name, email, and a password (at least 6 characters)",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    btnRegister.isEnabled = true
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid.orEmpty()
                        val userDoc = hashMapOf(
                            "uid" to uid,
                            "fullName" to fullName,
                            "email" to email,
                            "role" to selectedRole
                        )
                        if (uid.isNotEmpty()) {
                            db.collection("users").document(uid).set(userDoc)
                        }
                        Toast.makeText(this, "Account created as $selectedRole!", Toast.LENGTH_SHORT).show()
                        openDashboardForRole(selectedRole, fullName)
                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.localizedMessage ?: "Try again"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    private fun getSelectedRole(rgRole: RadioGroup): String {
        return when (rgRole.checkedRadioButtonId) {
            R.id.rbTutor -> "TUTOR"
            R.id.rbCoordinator -> "COORDINATOR"
            else -> "LEARNER"
        }
    }

    private fun openDashboardForRole(role: String, displayName: String) {
        val destination = when (role) {
            "TUTOR" -> TutorScheduleActivity::class.java
            "COORDINATOR" -> CoordinatorDashboardActivity::class.java
            else -> MainActivity::class.java
        }
        val intent = Intent(this, destination).apply {
            putExtra("EXTRA_USER_NAME", displayName)
            putExtra("EXTRA_USER_ROLE", role)
        }
        startActivity(intent)
        finish()
    }
}
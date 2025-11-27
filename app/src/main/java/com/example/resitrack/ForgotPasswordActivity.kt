package com.example.resitrack

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnReset = findViewById<Button>(R.id.btnReset)

        btnReset.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your registered email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()

                    }
                }
        }
    }
}

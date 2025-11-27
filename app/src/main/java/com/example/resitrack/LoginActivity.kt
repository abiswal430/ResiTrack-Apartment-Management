package com.example.resitrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgot = findViewById<TextView>(R.id.tvForgot)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Optionally bypass email-verified check for debugging:
        // (Uncomment to test login success even if user not verified)
        // if (auth.currentUser != null) {
        //     startActivity(Intent(this, HomeActivity::class.java))
        //     finish()
        // }

        // If you want the verified-only flow, use this:
        if (auth.currentUser != null && auth.currentUser!!.isEmailVerified) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            // User signed in but not verified
                            Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_LONG).show()
                            Log.w(TAG, "User signed in but email not verified: ${user?.email}")
                        }
                    } else {
                        // IMPORTANT: use correct interpolation to show real message
                        val msg = task.exception?.localizedMessage ?: "Unknown error"
                        Toast.makeText(this, "Login failed: $msg", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "signInWithEmail failed: $msg", task.exception)
                    }
                }
                .addOnFailureListener { e ->
                    // Extra safety to log low-level failures
                    Toast.makeText(this, "Login error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "signIn exception", e)
                }
        }

        tvForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}

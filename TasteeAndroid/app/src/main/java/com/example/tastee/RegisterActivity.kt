package com.example.tastee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.tastee.dto.LoginRequest
import com.example.tastee.dto.RegistrationRequest
import com.example.tastee.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToLogin()
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        registerType = intent.getStringExtra("REGISTER_TYPE") ?: "CUSTOMER"

        val root = findViewById<ConstraintLayout>(R.id.main)

        val views = listOf(
            findViewById<View>(R.id.nameInput),
            findViewById<View>(R.id.surnameInput),
            findViewById<View>(R.id.emailInput),
            findViewById<View>(R.id.usernameInput),
            findViewById<View>(R.id.passwordInput),
            findViewById<View>(R.id.repeatPassword)
        )

        views.forEach { view ->
            view.alpha = 0f
            view.translationY = 80f
        }

        findViewById<Button>(R.id.registerButton).setOnClickListener {
            registerUser()
        }

        root.post {
            TransitionManager.beginDelayedTransition(
                root,
                AutoTransition().apply { duration = 300 }
            )

            val constraintSet = ConstraintSet()
            constraintSet.clone(root)
            constraintSet.setGuidelinePercent(R.id.guideline, 0.05f)
            constraintSet.applyTo(root)
        }

        root.postDelayed({
            views.forEach { view ->
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .start()
            }
        }, 200)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun registerUser() {
        val role = when (registerType) {
            "BUSINESS" -> "ROLE_RESTAURANT"
            else -> "ROLE_CUSTOMER"
        }

        val request = RegistrationRequest(
            username = findViewById<EditText>(R.id.usernameInput).text.toString(),
            email = findViewById<EditText>(R.id.emailInput).text.toString(),
            password = findViewById<EditText>(R.id.passwordInput).text.toString(),
            repeatPassword = findViewById<EditText>(R.id.repeatPassword).text.toString(),
            name = findViewById<EditText>(R.id.nameInput).text.toString(),
            surname = findViewById<EditText>(R.id.surnameInput).text.toString(),
            role = role
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.authApi.register(request)

                val loginRequest = LoginRequest(
                    identifier = request.username,
                    password = request.password
                )

                val loginResponse = RetrofitClient.authApi.login(loginRequest)

                if (!loginResponse.isSuccessful) {
                    throw Exception("Automatic login failed: ${loginResponse.code()}")
                }

                val loginBody = loginResponse.body() ?: throw Exception("Invalid login response")

                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                val editor = prefs.edit()
                    .putBoolean("loggedIn", true)
                    .putString("username", request.username)
                    .putString("role", loginBody.role)

                if (registerType == "BUSINESS") {
                    editor.putBoolean("restaurantRegistrationInProgress", true)
                }

                editor.apply()

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration successful!",
                        Toast.LENGTH_LONG
                    ).show()

                    if (registerType == "BUSINESS") {
                        startActivity(Intent(this@RegisterActivity, RegisterBusinessActivity::class.java))
                        finish()
                    } else {
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegisterActivity,
                        e.message ?: "Registration failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
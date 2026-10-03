package com.example.tastee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.tastee.dto.LoginRequest
import com.example.tastee.network.RetrofitClient
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val token = result.accessToken.token
                Toast.makeText(this@LoginActivity, "Facebook Login Success", Toast.LENGTH_SHORT).show()
            }

            override fun onCancel() {
                Toast.makeText(this@LoginActivity, "Facebook login cancelled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@LoginActivity, "Facebook login error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })

        val animateFromLogout = intent.getBooleanExtra("ANIMATE_FROM_LOGOUT", false)
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val loggedIn = prefs.getBoolean("loggedIn", false)
        val role = prefs.getString("role", "ROLE_CUSTOMER")
        val restaurantRegistrationInProgress = prefs.getBoolean("restaurantRegistrationInProgress", false)

        if (loggedIn && restaurantRegistrationInProgress) {
            startActivity(Intent(this, RegisterBusinessActivity::class.java))
            finish()
            return
        }

        if (loggedIn && !restaurantRegistrationInProgress) {
            when (role) {
                "ROLE_DELIVERY" -> startActivity(Intent(this, MainDeliveryActivity::class.java))
                "ROLE_RESTAURANT" -> startActivity(Intent(this, RestaurantOwnerActivity::class.java))
                else -> startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)

            val socialLayout = findViewById<LinearLayout>(R.id.socialMediaLayout)
            val params = socialLayout.layoutParams as ConstraintLayout.LayoutParams
            params.bottomMargin = systemBars.bottom + 16
            socialLayout.layoutParams = params

            insets
        }

        findViewById<View>(R.id.facebookButton).setOnClickListener {
            loginWithFacebook()
        }

        val registerButton = findViewById<Button>(R.id.registerButton)
        val registerDeliveryButton = findViewById<Button>(R.id.registerDeliveryButton)
        val registerBusinessButton = findViewById<Button>(R.id.registerBusinessButton)

        registerButton.setOnClickListener {
            val views = listOf(
                findViewById<View>(R.id.usernameInput),
                findViewById<View>(R.id.passwordInput),
                findViewById<View>(R.id.logInButton),
                findViewById<View>(R.id.registerButton),
                findViewById<View>(R.id.registerBusinessButton),
                findViewById<View>(R.id.registerDeliveryButton),
                findViewById<View>(R.id.socialMediaLayout)
            )

            views.forEach { view ->
                view.animate().translationY(150f).alpha(0f).setDuration(300).start()
            }

            registerButton.postDelayed({
                val intent = Intent(this, RegisterActivity::class.java).apply {
                    putExtra("ROLE", "ROLE_CUSTOMER")
                }
                startActivity(intent)
                overridePendingTransition(0, 0)
            }, 300)
        }

        registerDeliveryButton.setOnClickListener {
            val views = listOf(
                findViewById<View>(R.id.usernameInput),
                findViewById<View>(R.id.passwordInput),
                findViewById<View>(R.id.logInButton),
                findViewById<View>(R.id.registerButton),
                findViewById<View>(R.id.registerBusinessButton),
                findViewById<View>(R.id.registerDeliveryButton),
                findViewById<View>(R.id.socialMediaLayout)
            )

            views.forEach { view ->
                view.animate().translationY(150f).alpha(0f).setDuration(300).start()
            }

            registerDeliveryButton.postDelayed({
                val intent = Intent(this, RegisterDeliveryActivity::class.java).apply {
                    putExtra("ROLE", "ROLE_DELIVERY")
                }
                startActivity(intent)
                overridePendingTransition(0, 0)
            }, 300)
        }

        registerBusinessButton.setOnClickListener {
            val loggedInNow = prefs.getBoolean("loggedIn", false)
            val views = listOf(
                findViewById<View>(R.id.usernameInput),
                findViewById<View>(R.id.passwordInput),
                findViewById<View>(R.id.logInButton),
                findViewById<View>(R.id.registerButton),
                findViewById<View>(R.id.registerDeliveryButton),
                findViewById<View>(R.id.registerBusinessButton),
                findViewById<View>(R.id.socialMediaLayout)
            )

            views.forEach { view ->
                view.animate().translationY(150f).alpha(0f).setDuration(300).start()
            }

            registerBusinessButton.postDelayed({
                if (loggedInNow) {
                    prefs.edit().putBoolean("restaurantRegistrationInProgress", true).apply()
                    startActivity(Intent(this, RegisterBusinessActivity::class.java))
                } else {
                    startActivity(Intent(this, RegisterActivity::class.java).apply {
                        putExtra("REGISTER_TYPE", "BUSINESS")
                    })
                }
                overridePendingTransition(0, 0)
            }, 300)
        }

        val root = findViewById<ConstraintLayout>(R.id.main)
        val socialLayout = findViewById<LinearLayout>(R.id.socialMediaLayout)

        socialLayout.alpha = 0f
        socialLayout.translationY = 80f

        val views = listOf(
            findViewById<View>(R.id.tasteeTextView),
            findViewById<View>(R.id.usernameInput),
            findViewById<View>(R.id.passwordInput),
            findViewById<View>(R.id.logInButton),
            findViewById<View>(R.id.registerBusinessButton),
            findViewById<View>(R.id.registerDeliveryButton),
            findViewById<View>(R.id.registerButton),
            socialLayout
        )

        views.forEach { view ->
            view.alpha = 0f
            view.translationY = 80f
        }

        root.post {
            val shouldAnimate = animateFromLogout || savedInstanceState == null
            if (shouldAnimate) {
                TransitionManager.beginDelayedTransition(root, AutoTransition().apply { duration = 300 })
                val constraintSet = ConstraintSet()
                constraintSet.clone(root)
                constraintSet.setGuidelinePercent(R.id.guideline, 0.05f)
                constraintSet.applyTo(root)
            }
        }

        root.postDelayed({
            views.forEachIndexed { index, view ->
                view.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(index * 50L).start()
            }
        }, 300)

        findViewById<Button>(R.id.logInButton).setOnClickListener {
            loginUser()
        }
    }

    override fun onResume() {
        super.onResume()

        val root = findViewById<ConstraintLayout>(R.id.main)
        val socialLayout = findViewById<LinearLayout>(R.id.socialMediaLayout)

        val views = listOf(
            findViewById<View>(R.id.tasteeTextView),
            findViewById<View>(R.id.usernameInput),
            findViewById<View>(R.id.passwordInput),
            findViewById<View>(R.id.logInButton),
            findViewById<View>(R.id.registerBusinessButton),
            findViewById<View>(R.id.registerDeliveryButton),
            findViewById<View>(R.id.registerButton),
            socialLayout
        )

        views.forEach { view ->
            view.alpha = 0f
            view.translationY = 80f
        }

        root.post {
            TransitionManager.beginDelayedTransition(root, AutoTransition().apply { duration = 300 })
            val constraintSet = ConstraintSet()
            constraintSet.clone(root)
            constraintSet.setGuidelinePercent(R.id.guideline, 0.05f)
            constraintSet.applyTo(root)
        }

        root.postDelayed({
            views.forEachIndexed { index, view ->
                view.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(index * 50L).start()
            }
        }, 100)
    }

    private fun loginUser() {
        val username = findViewById<EditText>(R.id.usernameInput).text.toString()
        val password = findViewById<EditText>(R.id.passwordInput).text.toString()

        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val request = LoginRequest(identifier = username, password = password)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.authApi.login(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {
                            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                            prefs.edit()
                                .putBoolean("loggedIn", true)
                                .putString("username", username)
                                .putString("role", loginResponse.role)
                                .apply()

                            Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                            when (loginResponse.role) {
                                "ROLE_DELIVERY" -> startActivity(Intent(this@LoginActivity, MainDeliveryActivity::class.java))
                                "ROLE_RESTAURANT" -> startActivity(Intent(this@LoginActivity, RestaurantOwnerActivity::class.java))
                                else -> startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            }
                            finish()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(
            this,
            callbackManager,
            listOf("email", "public_profile")
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
package com.example.tastee

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tastee.network.RetrofitClient

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        RetrofitClient.init(this)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().clear().apply()

        super.onCreate(savedInstanceState)

        window.decorView.setBackgroundColor(Color.BLACK)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.black)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContentView(R.layout.activity_welcome)

        val wave = findViewById<SimpleWaveView>(R.id.wave)
        wave.setWaveHeightPercent(0.10f)
        wave.setWaveCenterPercent(0.35f)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)

            val continueButton = findViewById<Button>(R.id.continueButton)
            val params = continueButton.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.bottomMargin = systemBars.bottom + 24
            continueButton.layoutParams = params

            insets
        }

        val continueButton = findViewById<Button>(R.id.continueButton)

        continueButton.setOnClickListener {
            val text = findViewById<TextView>(R.id.welcomeTextView)
            val views = listOf(text, continueButton)

            views.forEach { view ->
                view.animate()
                    .translationY(150f)
                    .alpha(0f)
                    .setDuration(300)
                    .start()
            }

            continueButton.postDelayed({
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }, 300)
        }
    }

    override fun onResume() {
        super.onResume()

        val text = findViewById<TextView>(R.id.welcomeTextView)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val views = listOf(text, continueButton)

        views.forEach { view ->
            view.alpha = 0f
            view.translationY = 80f
        }

        views.forEachIndexed { index, view ->
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(index * 100L)
                .start()
        }
    }
}
package com.date.fourth.name.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.date.fourth.name.R

class SplashActivity : AppCompatActivity() {
    lateinit var handler: Handler
    lateinit var runnable: Runnable
    private val SPLASH_TIME: Long = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContentView(R.layout.activity_splash)


        val spLogo = findViewById<ImageView>(R.id.spLogo)

        spLogo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(1000)
            .setStartDelay(1000).start()
        handler = Handler(Looper.myLooper()!!)
        runnable = Runnable {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finishAffinity()
        }
    }

    override fun onStart() {
        super.onStart()
        handler.postDelayed(runnable, SPLASH_TIME)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
    }
}
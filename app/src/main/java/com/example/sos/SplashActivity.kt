package com.example.sos

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R.*

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var myRunnable : Runnable
    private lateinit var handler: Handler
    private lateinit var progress : ProgressBar
    private lateinit var image : ImageView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_splash)

        progress = findViewById(id.progress)
        image = findViewById(id.image)

        handler = Handler()

        myRunnable = Runnable {
            // Things to be done
            startProgress()
        }

        Thread(myRunnable).start()
    }

    private fun startProgress(){
        for (i in 0..100){
            Thread.sleep(50)
            Log.d("SHOW", i.toString())
            progress.progress = i
        }
        val sharedPreference =  getSharedPreferences("SOS", Context.MODE_PRIVATE)
        val login = sharedPreference.getBoolean("login", false)
        if(login){
            startActivity(Intent(this, MainActivity::class.java))
        }else{
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        Thread().stop()
    }
}
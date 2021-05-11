package com.example.retrofit_musicapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.retrofit_musicapp.R
import com.example.retrofit_musicapp.ui.list_song.ListSongActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val intent = Intent(this, ListSongActivity::class.java)
            startActivity(intent)
            finish()
        }, 1500)
    }
}
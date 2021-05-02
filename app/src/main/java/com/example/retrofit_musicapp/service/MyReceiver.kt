package com.example.retrofit_musicapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_MUSIC
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_MUSIC_SERVICE
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ID_SONG

class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val actionMusic = intent?.getIntExtra(ACTION_MUSIC, 0)

        val intentService = Intent(context, MyService::class.java)
        intentService.putExtra(ACTION_MUSIC_SERVICE, actionMusic)
        context?.startService(intentService)
    }
}
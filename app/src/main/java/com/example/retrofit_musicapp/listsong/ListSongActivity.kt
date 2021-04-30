package com.example.retrofit_musicapp.listsong

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.retrofit_musicapp.MainActivity
import com.example.retrofit_musicapp.R
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_MUSIC
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_MUSIC_SERVICE
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_PAUSE
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_RESUME
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_START
import com.example.retrofit_musicapp.common.ServiceKey.Companion.OBJECT_SONG
import com.example.retrofit_musicapp.common.ServiceKey.Companion.SEND_DATA_TO_ACTIVITY
import com.example.retrofit_musicapp.common.ServiceKey.Companion.STATUS_PLAYER
import com.example.retrofit_musicapp.model.Song
import com.example.retrofit_musicapp.service.MyService

class ListSongActivity : AppCompatActivity() {

    private lateinit var mRecycleView: RecyclerView
    private lateinit var mListSongViewModel: ListSongViewModel
    private lateinit var imgSong:ImageView
    private lateinit var imgPlayOrPause:ImageView
    private lateinit var txtNameSong:TextView
    private lateinit var txtNameSinger:TextView
    private lateinit var mLayoutBottom:RelativeLayout
    private var mSong: Song? = null
    private var isPlaying: Boolean = true

    private val mReceiver = (object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle: Bundle? = intent?.extras
            mSong = bundle?.get(OBJECT_SONG) as Song?
            isPlaying = bundle?.getBoolean(STATUS_PLAYER)!!
            val actionMusic = bundle.getInt(ACTION_MUSIC)
            handleLayoutMusic(actionMusic)
        }

    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_song)
        mRecycleView = findViewById(R.id.mRecycleView)

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, IntentFilter(SEND_DATA_TO_ACTIVITY))

        imgSong = findViewById(R.id.imgSong)
        imgPlayOrPause = findViewById(R.id.imgPlayOrPause)
        txtNameSinger = findViewById(R.id.txtNameSinger)
        txtNameSong = findViewById(R.id.txtNameSong)
        mLayoutBottom = findViewById(R.id.mLayoutBottom)

        mRecycleView.layoutManager = LinearLayoutManager(this)
        mListSongViewModel = ViewModelProvider(this).get(ListSongViewModel::class.java)

        mListSongViewModel.gallerySongLiveData.observe(this, Observer { song ->
            mRecycleView.adapter = ListSongAdapter(song)
        })

    }

    fun handleLayoutMusic(action:Int){
        when(action){
            ACTION_START -> {
                mLayoutBottom.visibility = View.VISIBLE
                Log.d("show", "handleLayoutMusic: ${mSong?.urlSong}")
                showInfoSong(action)
                setStatusButtonPlayOrPause()
            }
            ACTION_PAUSE->setStatusButtonPlayOrPause()
            ACTION_RESUME->setStatusButtonPlayOrPause()
        }
    }

    private fun showInfoSong(action:Int) {
        if (mSong == null) {
            return
        }
        Glide.with(this).load(mSong?.urlImage).into(imgSong)
        txtNameSong.text = mSong?.nameSound
        txtNameSinger.text = mSong?.singer

        imgPlayOrPause.setOnClickListener {
            if (isPlaying){
                sendActionToService(ACTION_PAUSE)
            }else{
                sendActionToService(ACTION_RESUME)
            }
        }

        mLayoutBottom.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            val bundle = Bundle().apply {
                putSerializable(OBJECT_SONG,mSong)
                putBoolean(STATUS_PLAYER,isPlaying)
                putInt(ACTION_MUSIC,action)
            }
            intent.putExtras(bundle)
            startActivity(intent)
        }

     }

    private fun setStatusButtonPlayOrPause() {
        if (isPlaying){
            imgPlayOrPause.setImageResource(R.drawable.ic_play)
        }else{
            imgPlayOrPause.setImageResource(R.drawable.ic_pause)
        }
    }

    private fun sendActionToService(action: Int) {
        val intent = Intent(this,MyService::class.java)
        intent.putExtra(ACTION_MUSIC_SERVICE,action)
        startService(intent)
    }


}
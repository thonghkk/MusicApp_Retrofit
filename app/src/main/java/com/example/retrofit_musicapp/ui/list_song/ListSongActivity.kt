package com.example.retrofit_musicapp.ui.list_song

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.retrofit_musicapp.ui.PlayMusicActivity
import com.example.retrofit_musicapp.R
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_CLEAR
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
import com.example.retrofit_musicapp.service.MyService.Companion.mediaPlayer

class ListSongActivity : AppCompatActivity() {

    private lateinit var mRecycleView: RecyclerView
    private lateinit var mListSongViewModel: ListSongViewModel
    private lateinit var imgSong: ImageView
    private lateinit var imgPlayOrPause: ImageView
    private lateinit var txtNameSong: TextView
    private lateinit var txtNameSinger: TextView
    private lateinit var mLayoutBottom: LinearLayout
    private lateinit var progressBar: ProgressBar
    private var mSong: Song? = null
    private var isPlaying: Boolean = true

    private val mReceiver = (object : BroadcastReceiver() {
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

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mReceiver, IntentFilter(SEND_DATA_TO_ACTIVITY))

        imgSong = findViewById(R.id.imgSong)
        imgPlayOrPause = findViewById(R.id.imgPlayOrPause)
        txtNameSinger = findViewById(R.id.txtNameSinger)
        txtNameSong = findViewById(R.id.txtNameSong)
        mLayoutBottom = findViewById(R.id.mLayoutBottom)
        progressBar = findViewById(R.id.progressBar)

        mRecycleView.layoutManager = LinearLayoutManager(this)
        mListSongViewModel = ViewModelProvider(this).get(ListSongViewModel::class.java)

        mListSongViewModel.gallerySongLiveData.observe(this, Observer { song ->
            mRecycleView.adapter = ListSongAdapter(song)
        })
    }

    fun handleLayoutMusic(action: Int) {
        Log.d("show", "handleLayoutMusic: $action")

        //set Animation for image
        val x = ObjectAnimator.ofFloat(imgSong,"rotation",0f,360f)
        x.duration = 25000
        x.repeatCount = 10000
        when (action) {
            ACTION_START -> {
                mLayoutBottom.visibility = View.VISIBLE
                x.start()
                showInfoSong(action)
                setStatusButtonPlayOrPause()
            }
            ACTION_PAUSE -> {
                x.pause()
                setStatusButtonPlayOrPause()
             }
            ACTION_RESUME ->{
                x.resume()
                setStatusButtonPlayOrPause()
            }
            ACTION_CLEAR->{
                mLayoutBottom.visibility = View.GONE
                mediaPlayer?.release()
                mediaPlayer = null
            }
//            ACTION_NEXT_MUSIC -> nextMusic(action)
//            ACTION_PREV_MUSIC -> prevMusic(action)
        }
    }

    private fun prevMusic(action: Int) {
        showInfoSong(action)
        mediaPlayer?.release()
        mediaPlayer = null
        val intent = Intent(this, MyService::class.java)
        val bundle = Bundle()
        mListSongViewModel.gallerySongLiveData.observe(this, Observer { song ->
            for (i in song) {
                if (i.idSong == mSong?.idSong) {
                    Log.d("nameNext", "prevMusic: ${i.nameSound}")
                }
            }
        })
//        intent.putExtras(bundle)
//        startService(intent)
    }

    private fun nextMusic(action: Int) {
        showInfoSong(action)
        mediaPlayer?.release()
        mediaPlayer = null
        val intent = Intent(this, MyService::class.java)
        val bundle = Bundle()
        bundle.putSerializable(OBJECT_SONG, mSong)
        intent.putExtras(bundle)
        startService(intent)
    }

    private fun showInfoSong(action: Int) {
        if (mSong == null) {
            return
        }
        Glide.with(this).load(mSong?.urlImage).into(imgSong)
        txtNameSong.text = mSong?.nameSound
        txtNameSinger.text = mSong?.singer

        imgPlayOrPause.setOnClickListener {
            if (isPlaying) {
                sendActionToService(ACTION_PAUSE)
            } else {
                sendActionToService(ACTION_RESUME)
            }
        }

        mLayoutBottom.setOnClickListener {
            val intent = Intent(this, PlayMusicActivity::class.java)
            val bundle = Bundle().apply {
                putSerializable(OBJECT_SONG, mSong)
                putBoolean(STATUS_PLAYER, isPlaying)
                putInt(ACTION_MUSIC, action)
            }
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    private fun setStatusButtonPlayOrPause() {
        if (isPlaying) {
            imgPlayOrPause.setImageResource(R.drawable.ic_play)
        } else {
            imgPlayOrPause.setImageResource(R.drawable.ic_pause)
        }
    }

    private fun sendActionToService(action: Int) {
        val intent = Intent(this, MyService::class.java)
        intent.putExtra(ACTION_MUSIC_SERVICE, action)
        startService(intent)
    }
}
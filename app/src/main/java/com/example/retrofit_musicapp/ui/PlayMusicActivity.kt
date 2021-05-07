package com.example.retrofit_musicapp.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.retrofit_musicapp.R
import com.example.retrofit_musicapp.common.ServiceKey
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_CLEAR
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_MUSIC
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_PAUSE
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_RESUME
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_START
import com.example.retrofit_musicapp.common.ServiceKey.Companion.OBJECT_SONG
import com.example.retrofit_musicapp.common.ServiceKey.Companion.SEND_DATA_TO_ACTIVITY
import com.example.retrofit_musicapp.common.ServiceKey.Companion.STATUS_PLAYER
import com.example.retrofit_musicapp.model.Song
import com.example.retrofit_musicapp.service.MyService
import com.example.retrofit_musicapp.service.MyService.Companion.mediaPlayer

class PlayMusicActivity : AppCompatActivity() {

    private lateinit var mPlayer: ImageView
    private lateinit var imgSong: ImageView
    private lateinit var txtTimeStart: TextView
    private lateinit var txtTimeEnd: TextView
    private lateinit var txtNameSong: TextView
    private lateinit var txtNameSinger: TextView
    private lateinit var mSeekBar: SeekBar

    private var isPlaying = true

    private val mReceivers = (object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle: Bundle? = intent?.extras
            val mSong = bundle?.get(OBJECT_SONG) as Song?
            Log.d("song", "onReceive: ${mSong?.nameSound}")
            isPlaying = bundle?.getBoolean(STATUS_PLAYER)!!
            val actionMusic = bundle.getInt(ACTION_MUSIC)
            handleLayoutMusic(actionMusic, mSong)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music)

        mPlayer = findViewById(R.id.mPlayer)
        mSeekBar = findViewById(R.id.mSeekBar)
        txtTimeStart = findViewById(R.id.txtTimeStart)
        txtTimeEnd = findViewById(R.id.txtTimeEnd)
        txtNameSong = findViewById(R.id.txtNameSong)
        txtNameSinger = findViewById(R.id.txtNameSinger)
        imgSong = findViewById(R.id.imgSong)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mReceivers, IntentFilter(SEND_DATA_TO_ACTIVITY))

        //get bundle from list song
        val bundle: Bundle? = intent?.extras
        val song: Song? = bundle?.get(OBJECT_SONG) as Song?
        txtNameSinger.text = song?.singer
        txtNameSong.text = song?.nameSound
        Glide.with(this).load(song?.urlImage).into(imgSong)
        isPlaying = bundle?.getBoolean(STATUS_PLAYER)!!
        val actionMusic = bundle.getInt(ACTION_MUSIC)

        setStatusButtonPlayOrPause()
        //catch click event of user
        mPlayer.setOnClickListener {
            if (isPlaying) {
                sendActionToService(ACTION_PAUSE)
            } else {
                sendActionToService(ACTION_RESUME)
            }
        }
        handleLayoutMusic(actionMusic, song)
        //custom time
        positionBar()
    }

    private fun positionBar() {
        mSeekBar.max = mediaPlayer!!.duration
        mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        Thread(Runnable {
            while (mediaPlayer != null) {
                try {
                    val msg = Message()
                    msg.what = mediaPlayer!!.currentPosition
                    handler.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }).start()
    }

    @SuppressLint("HandlerLeak")
    var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val currentPosition = msg.what
            //update position bar
            mSeekBar.progress = currentPosition
            //update labels
            val elapsedTime = createTimeLabel(currentPosition)
            val remainingTime = createTimeLabel(mediaPlayer!!.duration - currentPosition)
            setTime(elapsedTime, remainingTime)
            Log.d(
                "TAG",
                "handleMessage: ${mediaPlayer!!.duration} and $remainingTime and ${msg.what}"
            )
            if (remainingTime == "0:00") {
                mPlayer.setImageResource(R.drawable.ic_pause)
                mediaPlayer?.pause()
                sendActionToService(ACTION_PAUSE)
            }
        }
    }

    fun setTime(elapsedTime: String, remainingTime: String) {
        txtTimeStart.text = elapsedTime
        txtTimeEnd.text = remainingTime
    }

    fun createTimeLabel(time: Int): String {
        var timeLabel = ""
        val min = time / 1000 / 60
        val sec = time / 1000 % 60

        timeLabel = "$min:"
        if (sec < 10) timeLabel += "0"
        timeLabel += sec
        return timeLabel
    }

    fun handleLayoutMusic(action: Int, song: Song?) {
        val myAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_indefinitely)
        imgSong.startAnimation(myAnim)
        when (action) {
            ACTION_START -> {
                setStatusButtonPlayOrPause()
            }
            ACTION_PAUSE -> {
                setStatusButtonPlayOrPause()
                myAnim.cancel()
            }
            ACTION_RESUME -> {
                setStatusButtonPlayOrPause()
                myAnim.start()
            }
            ACTION_CLEAR -> {
                mediaPlayer?.release()
                mediaPlayer = null
                myAnim.cancel()
                setTime("0:00", "0:00")
//                val intent = Intent(this, MyService::class.java)
//                val bundle = Bundle()
//                bundle.putSerializable(OBJECT_SONG, song)
//                intent.putExtras(bundle)
//                startService(intent)
            }
        }
    }

    private fun setStatusButtonPlayOrPause() {
        if (isPlaying) {
            mPlayer.setImageResource(R.drawable.ic_play)
        } else {
            mPlayer.setImageResource(R.drawable.ic_pause)
        }
    }

    private fun sendActionToService(action: Int) {
        val intent = Intent(this, MyService::class.java)
        intent.putExtra(ServiceKey.ACTION_MUSIC_SERVICE, action)
        startService(intent)
    }
}
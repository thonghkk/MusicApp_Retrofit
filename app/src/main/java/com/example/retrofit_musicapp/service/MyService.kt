package com.example.retrofit_musicapp.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.retrofit_musicapp.R
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_CLEAR
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_MUSIC
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_MUSIC_SERVICE
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_NEXT_MUSIC
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_PAUSE
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_PREV_MUSIC
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_RESUME
import com.example.retrofit_musicapp.common.ServiceKey.Companion.ACTION_START
import com.example.retrofit_musicapp.common.ServiceKey.Companion.CHANNEL_ID
import com.example.retrofit_musicapp.common.ServiceKey.Companion.OBJECT_SONG
import com.example.retrofit_musicapp.common.ServiceKey.Companion.SEND_DATA_TO_ACTIVITY
import com.example.retrofit_musicapp.common.ServiceKey.Companion.STATUS_PLAYER
import com.example.retrofit_musicapp.model.Song
import com.example.retrofit_musicapp.ui.PlayMusicActivity

open class MyService : Service() {
    companion object {
        var mediaPlayer: MediaPlayer? = null
    }

    private var isPlaying = true
    private var mSong: Song? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("TAG", "My Service started ! ")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle: Bundle? = intent?.extras
        val song: Song? = bundle?.get(OBJECT_SONG) as Song?

        val actionMusic: Int = intent?.getIntExtra(ACTION_MUSIC_SERVICE, 0)!!

        handleActionMusic(actionMusic)
        Log.d("action", "onStartCommand: $actionMusic")

        if (song != null) {
            mSong = song
            //sendNotification(mSong, actionMusic)
            notificationChannel(mSong, actionMusic)
            startMusic(mSong)
        }

        return START_NOT_STICKY
    }

    private fun startMusic(song: Song?) {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(song?.urlSong)
            prepare()
            start()
        }
        isPlaying = true
        sendActionToActivity(ACTION_START)
    }

    private fun handleActionMusic(action: Int) {
        when (action) {
            ACTION_PAUSE -> actionPause(action)
            ACTION_RESUME -> actionResume(action)
            ACTION_CLEAR -> actionClear(action)
        }
    }

    private fun actionPause(action: Int) {
        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
            //sendNotification(mSong, action)
            notificationChannel(mSong, action)
            sendActionToActivity(ACTION_PAUSE)
        }
    }

    private fun actionResume(action: Int) {
        if (!isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
            //sendNotification(mSong, action)
            notificationChannel(mSong, action)
            sendActionToActivity(ACTION_RESUME)
        }
    }

    private fun actionClear(action: Int) {
        stopSelf()
        isPlaying = false
        sendActionToActivity(ACTION_CLEAR)
        sendNotification(mSong, action)
    }

    private fun sendNotification(song: Song?, action: Int) {
        val intent = Intent(this, PlayMusicActivity::class.java)
        val bundle = Bundle().apply {
            putSerializable(OBJECT_SONG, mSong)
            putBoolean(STATUS_PLAYER, isPlaying)
            putInt(ACTION_MUSIC, action)
        }
        intent.putExtras(bundle)

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        //mapping all properties
        val remoteView = RemoteViews(packageName, R.layout.item_notification_music)
        remoteView.setTextViewText(R.id.txtNameSong, song?.nameSound!!)
        remoteView.setTextViewText(R.id.txtNameSinger, song.singer)
        remoteView.setImageViewResource(R.id.imgPlayOrPause, R.drawable.ic_play)
        Log.d("url", "error ${song.urlImage}")
        val url = song.urlImage

        Glide.with(this).asBitmap().load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    remoteView.setImageViewBitmap(R.id.imgSongNotification, resource)

                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })

        //catch event action
        if (!isPlaying) {
            remoteView.setOnClickPendingIntent(
                R.id.imgPlayOrPause,
                getPendingIntent(this, ACTION_RESUME)
            )
            remoteView.setImageViewResource(R.id.imgPlayOrPause, R.drawable.ic_pause)
        } else {
            remoteView.setOnClickPendingIntent(
                R.id.imgPlayOrPause,
                getPendingIntent(this, ACTION_PAUSE)
            )
            remoteView.setImageViewResource(R.id.imgPlayOrPause, R.drawable.ic_play)
        }

        //catch event next or back music
        remoteView.setOnClickPendingIntent(
            R.id.skip_prev,
            getPendingIntent(this, ACTION_PREV_MUSIC)
        )
        remoteView.setOnClickPendingIntent(
            R.id.skip_next,
            getPendingIntent(this, ACTION_NEXT_MUSIC)
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteView)
            .setSound(null)
            .build()
        startForeground(1, notification)
    }


    private fun getPendingIntent(context: Context, action: Int): PendingIntent {
        val intent = Intent(this, MyReceiver::class.java)
        intent.putExtra(ACTION_MUSIC, action)
        return PendingIntent.getBroadcast(
            context.applicationContext,
            action,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun sendActionToActivity(action: Int) {
        val intent = Intent(SEND_DATA_TO_ACTIVITY)
        val bundle = Bundle().apply {
            putSerializable(OBJECT_SONG, mSong)
            putBoolean(STATUS_PLAYER, isPlaying)
            putInt(ACTION_MUSIC, action)
        }
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d("show", "handleLayoutMusic: begin_111")

    }

    private fun notificationChannel(song: Song?, action: Int) {
        //handle when user click on notification move to main activity
        val intent = Intent(this, PlayMusicActivity::class.java)
        val bundle = Bundle().apply {
            putSerializable(OBJECT_SONG, mSong)
            putBoolean(STATUS_PLAYER, isPlaying)
            putInt(ACTION_MUSIC, action)
        }
        intent.putExtras(bundle)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val mediaSessionCompat = MediaSessionCompat(this, "tag")

        //create notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_)
            .setContentTitle(song?.nameSound!!)
            .setContentText(song.singer)
            .setContentIntent(pendingIntent)
            .setSubText(song.nameSound)
            //set action
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSessionCompat.sessionToken)
            )

        //launch image on notification
        Glide.with(this)
            .asBitmap()
            .load(song.urlImage)
            .fitCenter()
            .into(object : CustomTarget<Bitmap>(100, 100) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    notificationBuilder.setLargeIcon(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

        //catch event click of user
        if (isPlaying) {
            notificationBuilder
                .addAction(R.drawable.ic_skip_back, "", null)
                .addAction(
                    R.drawable.ic_play, "", getPendingIntent(
                        this,
                        ACTION_PAUSE
                    )
                )
                .addAction(R.drawable.ic_skip_next, "", null)
                .addAction(
                    R.drawable.ic_clear, "", getPendingIntent(
                        this,
                        ACTION_CLEAR
                    )
                )
        } else {
            notificationBuilder
                .addAction(R.drawable.ic_skip_back, "", null)
                .addAction(
                    R.drawable.ic_pause, "", getPendingIntent(
                        this,
                        ACTION_RESUME
                    )
                )
                .addAction(R.drawable.ic_skip_next, "", null)
                .addAction(
                    R.drawable.ic_clear, "", getPendingIntent(
                        this,
                        ACTION_CLEAR
                    )
                )
        }
        val notification = notificationBuilder.build()
        startForeground(1, notification)

    }
}
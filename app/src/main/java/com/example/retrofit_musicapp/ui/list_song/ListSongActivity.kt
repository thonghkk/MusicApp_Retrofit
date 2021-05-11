package com.example.retrofit_musicapp.ui.list_song

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
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
import com.example.retrofit_musicapp.ui.PlayMusicActivity

class ListSongActivity : AppCompatActivity() {

    private lateinit var mRecycleView: RecyclerView
    private lateinit var mListSongViewModel: ListSongViewModel
    private lateinit var imgSong: ImageView
    private lateinit var imgPlayOrPause: ImageView
    private lateinit var txtNameSong: TextView
    private lateinit var txtNameSinger: TextView
    private lateinit var mLayoutBottom: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var mImageSlider: ImageSlider
    private lateinit var mSearchView: SearchView

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
        mImageSlider = findViewById(R.id.mImageSlider)
        mSearchView = findViewById(R.id.mSearchView)

        mRecycleView.layoutManager = LinearLayoutManager(this)
        mListSongViewModel = ViewModelProvider(this).get(ListSongViewModel::class.java)

        val mListImage = mutableListOf<SlideModel>()

        mListSongViewModel.gallerySongLiveData.observe(this, { song ->
            val songAdapter = ListSongAdapter(song)
            mRecycleView.adapter = songAdapter
            searchSong(songAdapter)
        })

        mListImage.add(SlideModel("https://zmp3-photo-fbcrawler.zadn.vn/avatars/9/2/92663468154b888bb69c5dfa7915ea9f_1490961913.jpg"))
        mListImage.add(SlideModel("https://i1.sndcdn.com/artworks-000281765348-xvrwga-t500x500.jpg"))
        mListImage.add(SlideModel("https://avatar-nct.nixcdn.com/playlist/2014/10/13/b/5/b/e/1413212552670_500.jpg"))
        mListImage.add(SlideModel("https://25.media.tumblr.com/tumblr_mywet1zUzO1qfxq87o1_1388874998_cover.jpg"))

        mImageSlider.setImageList(mListImage,ScaleTypes.CENTER_CROP)
    }

    fun handleLayoutMusic(action: Int) {
        Log.d("show", "handleLayoutMusic: $action")

        //set Animation for image
        val x = ObjectAnimator.ofFloat(imgSong, "rotation", 0f, 360f)
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
            ACTION_RESUME -> {
                x.resume()
                setStatusButtonPlayOrPause()
            }
            ACTION_CLEAR -> {
                mLayoutBottom.visibility = View.GONE
                mediaPlayer?.release()
                mediaPlayer = null
            }
//            ACTION_NEXT_MUSIC -> nextMusic(action)
//            ACTION_PREV_MUSIC -> prevMusic(action)
        }
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

    private fun searchSong(listSongAdapter: ListSongAdapter){
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                 listSongAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                listSongAdapter.filter.filter(newText)
                return false
             }

        })
    }

}
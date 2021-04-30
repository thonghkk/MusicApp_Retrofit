package com.example.retrofit_musicapp.listsong

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.retrofit_musicapp.MainActivity
import com.example.retrofit_musicapp.R
import com.example.retrofit_musicapp.common.ServiceKey.Companion.OBJECT_SONG
import com.example.retrofit_musicapp.model.Song
import com.example.retrofit_musicapp.service.MyService

class ListSongAdapter(private val sounds: List<Song>) :
    RecyclerView.Adapter<ListSongAdapter.ViewHolder>() {

    class ViewHolder(soundView: View) : RecyclerView.ViewHolder(soundView) {

        private val mNameSound = soundView.findViewById<TextView>(R.id.mNameSound)
        private val mNameSinger = soundView.findViewById<TextView>(R.id.mNameSinger)
        private val mImage = soundView.findViewById<ImageView>(R.id.mImg)
        private val mLinear = soundView.findViewById<LinearLayout>(R.id.mLinear)

        fun binds(song: Song) {
            val urlImage = song.urlImage
            val urlSong = song.urlSong
            Glide.with(itemView).load(urlImage)
                .apply(RequestOptions.circleCropTransform())
                .into(mImage)
            mNameSound.text = song.nameSound
            mNameSinger.text = song.singer

            mLinear.setOnClickListener {
                val intent = Intent(itemView.context,MyService::class.java)
                val bundle = Bundle()
                bundle.putSerializable(OBJECT_SONG, song)
                intent.putExtras(bundle)
                itemView.context.startService(intent)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binds(sounds[position])
    }

    override fun getItemCount() = sounds.size


}
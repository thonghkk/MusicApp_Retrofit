package com.example.retrofit_musicapp.listsong

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.retrofit_musicapp.api.SongFetch.Companion.songApi
import com.example.retrofit_musicapp.model.Song
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListSongViewModel:ViewModel() {

    var gallerySongLiveData: MutableLiveData<List<Song>>
    val data: MutableLiveData<List<Song>>

    init {
        gallerySongLiveData = fetch()
        data = fetch()
    }

    private fun fetch(): MutableLiveData<List<Song>> {

        val responseLiveData: MutableLiveData<List<Song>> = MutableLiveData()
        val flickRequest: Call<List<Song>> = songApi.fetchSound()

        flickRequest.enqueue(object : Callback<List<Song>> {
            override fun onResponse(call: Call<List<Song>>, response: Response<List<Song>>) {

                val soundResponse: List<Song>? = response.body()
                val gallerySound: List<Song> = soundResponse ?: mutableListOf()

                responseLiveData.value = gallerySound

                Log.e("TAG", "Response success")
            }

            override fun onFailure(call: Call<List<Song>>, t: Throwable) {
                Log.e("TAG", "Error :" + t.message)

            }
        })
        return responseLiveData
    }

}
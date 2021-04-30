package com.example.retrofit_musicapp.api

import com.example.retrofit_musicapp.model.Song
import retrofit2.Call
import retrofit2.http.GET

interface SongApi {
    @GET("server/listsong.php")
    fun fetchSound(): Call<List<Song>>
}
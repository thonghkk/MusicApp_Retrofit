package com.example.retrofit_musicapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SongFetch {
    companion object{
        val songApi: SongApi
        init {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("https://androidwebapi.000webhostapp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            songApi = retrofit.create(SongApi::class.java)
        }
    }
}
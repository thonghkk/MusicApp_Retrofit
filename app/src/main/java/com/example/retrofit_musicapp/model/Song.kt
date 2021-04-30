package com.example.retrofit_musicapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Song(
    @SerializedName("tenBaiHat")
    val nameSound:String,
    @SerializedName("hinhBaiHat")
    val urlImage:String,
    @SerializedName("caSy")
    val singer:String,
    @SerializedName("linkBaiHat")
    val urlSong:String
): Serializable
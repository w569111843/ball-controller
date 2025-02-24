package com.example.ball.util

import com.example.ball.model.FileItem
import com.example.ball.model.MuteSetting
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @GET("/filelist")
    suspend fun getFileList(): List<FileItem>

    @GET("/deleteUploadFile")
    suspend fun removeFile(@Query("fileName") fileName: String)

    @GET("/settingFileName")
    suspend fun settingFileName(@Query("fileName") fileName: String)

    @GET("/getSDCardSpace")
    suspend fun getSDCardSpace(): Long

    @GET("/play-video")
    suspend fun playVideo()

    @GET("/pause-video")
    suspend fun pauseVideo()

    @GET("/settingMute")
    suspend fun settingMute()

    @GET("/setVolumeLevel")
    suspend fun setVolumeLevel(@Query("level") level: Int)

    @GET("/getVolumeSetting")
    suspend fun getVolumeSetting(): MuteSetting

    @Multipart
    @POST("/upload") // 替换为你的上传端点
    suspend fun uploadVideo(
        @Part file: MultipartBody.Part
    )

    @GET("/getBatSoc")
    suspend fun getBatSoc(): Double
}
package com.example.ball.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient{
    private const val BASE_URL = "http://192.168.1.1"

    private val retrofit: Retrofit by lazy {
        // 创建日志拦截器
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 设置日志级别
        }

        // 创建 OkHttpClient 并添加日志拦截器
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(180, TimeUnit.SECONDS) // 连接超时 3 分钟
            .readTimeout(180, TimeUnit.SECONDS)    // 读取超时 3 分钟
            .writeTimeout(180, TimeUnit.SECONDS)
            .build()

        // 创建 Retrofit 实例
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 设置 baseUrl
            .client(okHttpClient) // 设置 OkHttpClient
            .addConverterFactory(GsonConverterFactory.create()) // 添加 Gson 转换器
            .build()
    }

    // 你可以通过这个方法获取你的 ApiService 实例
    fun getApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    suspend fun <T> makeApiCall(call: suspend () -> T): T? {
        return try {
            // 在 IO 线程中进行网络请求
            withContext(Dispatchers.IO) {
                call()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

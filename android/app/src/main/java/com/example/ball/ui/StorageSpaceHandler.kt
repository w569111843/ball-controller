package com.example.ball.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.ball.util.ApiClient
import com.example.ball.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class StorageSpaceHandler(private val context: Context) {
    private val totalStorage: Long = 1024 * 1024 * 1024 * 16L
    private var usedStorage: Long = 0L
    private val totalStorageStr = "16 GB";

    fun getStorageSpace() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiClient.makeApiCall {
                ApiClient.getApiService().getSDCardSpace()
            }
            result?.let {
                usedStorage = it
                var str = totalStorageStr
                str = formatSize(usedStorage) + " / " + str
                val usedStorageBar: View = (context as Activity).findViewById(R.id.used_storage_bar)
                val parentWidth = (usedStorageBar.parent as View).width
                val newWidth = (parentWidth * usedStorage / totalStorage).toInt()
                val layoutParams = usedStorageBar.layoutParams
                layoutParams.width = newWidth
                usedStorageBar.layoutParams = layoutParams
                val usedStorageText: TextView = context.findViewById(R.id.used_storage_text)
                usedStorageText.text = str
            } ?: run {
                Toast.makeText(context, "获取存储空间失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatSize(size: Long): String {
        val GB = 1024L * 1024 * 1024
        val MB = 1024L * 1024
        val KB = 1024L

        return when {
            size >= GB -> {
                // 使用 Double 来保留小数点后两位，避免整数除法的精度问题
                String.format(Locale.CHINA, "%.2f GB", size.toDouble() / GB)
            }
            size >= MB -> {
                String.format(Locale.CHINA, "%.2f MB", size.toDouble() / MB)
            }
            else -> {
                String.format(Locale.CHINA, "%.2f KB", size.toDouble() / KB)
            }
        }
    }

}
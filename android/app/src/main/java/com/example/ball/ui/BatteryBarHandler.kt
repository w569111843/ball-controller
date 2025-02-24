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

class BatteryBarHandler(private val context: Context) {
    fun getBattery() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiClient.makeApiCall {
                ApiClient.getApiService().getBatSoc()
            }
            result?.let {
                val batteryBar: View = (context as Activity).findViewById(R.id.battery_bar)
                val parentWidth = (batteryBar.parent as View).width
                val newWidth = (parentWidth * it).toInt()
                val layoutParams = batteryBar.layoutParams
                layoutParams.width = newWidth
                batteryBar.layoutParams = layoutParams
                val usedStorageText: TextView = context.findViewById(R.id.battery_text)
                usedStorageText.text = String.format(Locale.CHINA, "剩余电量:%.2f%%", it)
            } ?: run {
                Toast.makeText(context, "获取电量失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

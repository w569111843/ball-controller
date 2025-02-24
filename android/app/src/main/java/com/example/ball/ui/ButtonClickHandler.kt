package com.example.ball.ui


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ball.util.ApiClient
import com.example.ball.R
import com.example.ball.model.MuteSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import com.example.ball.adapter.FileAdapter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.InputStream

class ButtonClickHandler(private val context: Context, private val fileAdapter: FileAdapter) {

    private lateinit var pickVideoLauncher: ActivityResultLauncher<Intent>

    // 打开文件选择器
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*" // 限制选择视频文件
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickVideoLauncher.launch(intent)
    }

    private fun handleSelectedVideo(uri: Uri) {
        try {
            val fileName = getFileNameFromUri(uri)
            val mimeType = context.contentResolver.getType(uri)

            // 直接通过 Uri 获取 InputStream
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Toast.makeText(context, "无法读取文件", Toast.LENGTH_SHORT).show()
                return
            }

            // 直接上传流
            uploadVideo(inputStream, fileName, mimeType)
        } catch (e: Exception) {
            Toast.makeText(context, "无法读取文件: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        return when (uri.scheme) {
            "content" -> {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    } else {
                        "unknown_file"
                    }
                } ?: "unknown_file"
            }
            "file" -> File(uri.path).name
            else -> "unknown_file"
        }
    }


    private fun uploadVideo(inputStream: InputStream, fileName: String, mimeType: String?) {
        uploadButton?.text = "上传中"
        uploadButton?.isEnabled = false
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val fileBytes = inputStream.readBytes()

                // 创建 RequestBody
                val requestBody =fileBytes.toRequestBody(mimeType?.toMediaTypeOrNull())

                // 创建 MultipartBody.Part
                val body = MultipartBody.Part.createFormData("file", fileName, requestBody)

                // 调用 API 上传文件
                val result = ApiClient.makeApiCall {
                    ApiClient.getApiService().uploadVideo(body)
                }

                result?.let {
                    uploadButton?.text = "上传"
                    uploadButton?.isEnabled = true
                    Toast.makeText(context, "上传成功", Toast.LENGTH_SHORT).show()
                    fileAdapter.loadItems(null)
                } ?: run {
                    uploadButton?.text = "上传"
                    uploadButton?.isEnabled = true
                    Toast.makeText(context, "上传失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uploadButton?.text = "上传"
                uploadButton?.isEnabled = true
                Toast.makeText(context, "上传错误: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var muteSetting: MuteSetting = MuteSetting(true, 0)

    fun getMuteSetting() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiClient.makeApiCall {
                ApiClient.getApiService().getVolumeSetting()
            }
            result?.let {
                muteSetting = it
                var buttonText = "静音"
                if (muteSetting.isMuted) {
                    buttonText = "取消静音"
                }
                muteButton?.text = buttonText
                seekBar?.progress = muteSetting.level
            } ?: run {
                Toast.makeText(context, "获取静音设置失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var muteButton: Button? = null
    private var seekBar: SeekBar? = null
    private var uploadButton: Button? = null

    fun initialize() {
        pickVideoLauncher = (context as AppCompatActivity).registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    // 处理选择的文件
                    handleSelectedVideo(uri)
                }
            }
        }
        uploadButton = (context as Activity).findViewById(R.id.upload_button)
        val playButton: Button = context.findViewById(R.id.play_button)
        val pauseButton: Button = context.findViewById(R.id.pause_button)
        muteButton = context.findViewById(R.id.mute_voice_button)
        seekBar = context.findViewById(R.id.seekBar)

        uploadButton?.setOnClickListener { openFilePicker() } // 修改为打开文件选择器
        playButton.setOnClickListener { play() }
        pauseButton.setOnClickListener { pause() }
        muteButton?.setOnClickListener { mute() }
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                muteSetting.level = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                settingVoiceLevel()
            }
        })
    }

    private fun settingVoiceLevel(){
        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiClient.makeApiCall {
                ApiClient.getApiService().setVolumeLevel(muteSetting.level)
            }
            result?.let {
                Toast.makeText(context, "设置音量成功", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "设置音量失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun play() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiClient.makeApiCall {
                ApiClient.getApiService().playVideo()
            }
            result?.let {
                Toast.makeText(context, "播放成功", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "播放失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pause() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiClient.makeApiCall {
                ApiClient.getApiService().pauseVideo()
            }
            result?.let {
                Toast.makeText(context, "暂停成功", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "暂停失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mute() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiClient.makeApiCall {
                ApiClient.getApiService().settingMute()
            }
            result?.let {
                muteSetting.isMuted = !muteSetting.isMuted
                var buttonText = "静音"
                if (muteSetting.isMuted) {
                    buttonText = "取消静音"
                }
                muteButton?.text = buttonText
                Toast.makeText(context, "$buttonText 成功", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "设置失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
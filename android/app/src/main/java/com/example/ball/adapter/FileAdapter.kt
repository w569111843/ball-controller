package com.example.ball.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ball.util.ApiClient
import com.example.ball.ui.common.SwipeDragLayout
import com.example.ball.model.FileItem
import com.example.ball.R
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class FileAdapter(
    private val context: Context,
    private val fileItems: MutableList<FileItem>
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false)
        return FileViewHolder(view)
    }

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val item = fileItems[position]
        holder.fileName.text = item.name
        holder.fileCreationTime.text = sdf.format(item.creationTime * 1000)
        holder.fileSize.text = formatSize(item.size)
        holder.delButton.setOnClickListener{
            deleteItem(position, item.name)
        }
        holder.playButton.setOnClickListener{
            playItem(item.name, holder.item)
        }
    }

    override fun getItemCount(): Int {
        return fileItems.size
    }

    private fun formatSize(size: Double): String{
        return if(size >= 1024 * 1024 * 1024) {
            String.format(Locale.CHINA, "%.2f GB", size / (1024 * 1024 * 1024))
        } else if(size >=  1024 * 1024) {
            String.format(Locale.CHINA,"%.2f MB", size / (1024 * 1024))
        } else {
            String.format(Locale.CHINA,"%.2f KB", size / 1024)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadItems(refreshLayout: RefreshLayout?) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiClient.makeApiCall {
                ApiClient.getApiService().getFileList()
            }
            result?.let {
                fileItems.clear()
                for (item in it) {
                    fileItems.add(FileItem(item.name, item.creationTime, item.size))
                }
                notifyDataSetChanged()
                refreshLayout?.finishRefresh(2000)
            } ?: run {
                // 请求失败，处理错误
                showError("获取文件列表失败")
                refreshLayout?.finishRefresh(false)
            }
        }
    }


    // 删除项
    private fun deleteItem(position: Int, fileName:String) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiClient.makeApiCall {
                ApiClient.getApiService().removeFile(fileName)
            }
            result?.let {
                fileItems.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
            } ?: run {
                // 请求失败，处理错误
                showError("删除失败")
            }
        }
    }

    // 播放项
    private fun playItem(fileName:String, item: SwipeDragLayout) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiClient.makeApiCall {
                ApiClient.getApiService().settingFileName(fileName)
            }
            result?.let {
                Toast.makeText(context, "播放成功", Toast.LENGTH_SHORT).show()
                item.close()
            } ?: run {
                // 请求失败，处理错误
                showError("播放失败")
            }
        }
    }

    class FileViewHolder(itemView: View, ) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.fileName)
        val fileCreationTime: TextView = itemView.findViewById(R.id.fileCreationTime)
        val fileSize: TextView = itemView.findViewById(R.id.fileSize)
        val delButton: Button = itemView.findViewById(R.id.btn_delete)
        val playButton: Button = itemView.findViewById(R.id.btn_play)
        val item: SwipeDragLayout = itemView as SwipeDragLayout
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
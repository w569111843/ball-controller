package com.example.ball.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ball.R
import com.example.ball.adapter.FileAdapter
import com.example.ball.model.FileItem
import com.scwang.smart.refresh.layout.api.RefreshLayout


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private lateinit var storageSpaceHandler: StorageSpaceHandler
    private lateinit var buttonClickHandler: ButtonClickHandler
    private lateinit var batteryBarHandler: BatteryBarHandler
    private val fileItems = mutableListOf<FileItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 设置操作栏

        storageSpaceHandler = StorageSpaceHandler(this)
        // 设置文件列表
        recyclerView = findViewById(R.id.file_list_recycler_view)
        fileAdapter = FileAdapter(this, fileItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = fileAdapter
        val refreshLayout =  findViewById<View>(R.id.refreshLayout) as RefreshLayout
        refreshLayout.setOnRefreshListener { refreshlayout ->
            fileAdapter.loadItems(refreshlayout)
        }
        buttonClickHandler = ButtonClickHandler(this, fileAdapter)
        buttonClickHandler.initialize()

        batteryBarHandler = BatteryBarHandler(this)

        fileAdapter.loadItems(null)
        buttonClickHandler.getMuteSetting()
        storageSpaceHandler.getStorageSpace()
        batteryBarHandler.getBattery()
    }

    override fun onStart() {
        super.onStart()
    }
}
package com.fxf.slideContainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_list.*
import java.util.*

class ListFragment : Fragment() {
    private var rootView: View? = null

    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null){
            rootView = inflater.inflate(R.layout.fragment_list, container, false)
        }
        listView = rootView!!.findViewById(R.id.listView)
        initListData()
        return rootView
    }

    private fun initListData() {
        val list = ArrayList<String>()
        for (i in 0..99) {
            list.add("第 " + i + "项")
        }

        val maps =
            ArrayList<Map<String, Any?>>()
        for (i in 0..19) {
            val itemData: MutableMap<String, Any?> =
                HashMap()
            itemData["pic"] = R.mipmap.ic_launcher
            itemData["text"] = "content$i"
            maps.add(itemData)
        }
        //准备构造SimpleAdapter的参数
        //准备构造SimpleAdapter的参数
        val from = arrayOf("pic", "text")
        val to = intArrayOf(R.id.list_iv, R.id.item_tv)

        //1.新建数据适配器  // 2.添加数据源到适配器
        //1.新建数据适配器  // 2.添加数据源到适配器
        val adapter = SimpleAdapter(context, maps, R.layout.list_item, from, to)

        listView.setAdapter(adapter)
    }
}
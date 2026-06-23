package com.example.futmatchapp.vista

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.view.Gravity
import androidx.core.content.ContextCompat

class GalleryAdapter(private val context: Context, private val dataSource: List<String>) : BaseAdapter() {

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val textView = (convertView as? TextView) ?: TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200
            )
            gravity = Gravity.CENTER
            textSize = 14f
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
            setBackgroundResource(android.R.drawable.dialog_holo_dark_frame)
            setPadding(8, 8, 8, 8)
        }

        textView.text = dataSource[position]
        return textView
    }
}
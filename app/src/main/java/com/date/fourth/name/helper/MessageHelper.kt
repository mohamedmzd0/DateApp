package com.date.fourth.name.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.date.fourth.name.R

class MessageHelper {
    companion object {

        fun toast(context: Context, text: String) {
            if (context == null)
                return
            if (text.isNullOrEmpty())
                return
            val toast = Toast(context)
            toast.duration = Toast.LENGTH_SHORT
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater.inflate(R.layout.toast_message, null)
            val message: TextView = view.findViewById(R.id.message)
            message.text = text
            toast.view = view
            toast.show()
        }
    }
}
package com.example.dailynews.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.dailynews.R

object EditTextUtils {
    fun setFocusChangeListener(context: Context, editText: EditText, icon: Drawable?) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                icon?.setTint(ContextCompat.getColor(context, R.color.my_light_active))
            } else {
                icon?.setTint(ContextCompat.getColor(context, R.color.black))
            }
            editText.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        }
    }
}
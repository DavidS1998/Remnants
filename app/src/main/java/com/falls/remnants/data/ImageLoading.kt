package com.falls.remnants.data

import android.graphics.Color.parseColor
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.alpha
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import timber.log.Timber


@BindingAdapter("ImageUrl")
fun bindPosterImage(imgView: ImageView, imgUrl: String) {
    imgUrl.let { posterPath ->
        Glide
            .with(imgView)
            .load(posterPath)
            .into(imgView)
    }
}

@BindingAdapter("SetColorAccent")
fun setColor(textView: TextView, color: String) {
    if (color.isNotEmpty()) {
        textView.setTextColor(parseColor(color))
    } else {
        textView.setTextColor(parseColor("#FFFFFF"))
    }
}

@BindingAdapter("SetColorBackground")
fun setBackground(button: Button, color: String) {
    if (color.isNotEmpty()) {
        button.setBackgroundColor(parseColor(color))
    } else {
        button.setBackgroundColor(parseColor("#FFFFFF"))
    }
}
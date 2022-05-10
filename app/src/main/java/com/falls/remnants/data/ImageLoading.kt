package com.falls.remnants.data

import android.graphics.Color.parseColor
import android.widget.ImageView
import android.widget.TextView
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
    val addTransparency = StringBuilder(color).insert(1, "60").toString()
    val bgColor = parseColor(addTransparency)
    textView.setBackgroundColor(bgColor)
}
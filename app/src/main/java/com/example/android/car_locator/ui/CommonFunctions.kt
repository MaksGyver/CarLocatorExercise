package com.example.android.car_locator.ui

import android.graphics.drawable.GradientDrawable
import android.widget.TextView

fun setColorOfTextView(tvColorValue: TextView, color: Int) {
    val gradDrawable = GradientDrawable()
    gradDrawable.setColor(color)
    gradDrawable.cornerRadius = 5f
    gradDrawable.setStroke(1, -0x1000000)
    tvColorValue.background = gradDrawable
}
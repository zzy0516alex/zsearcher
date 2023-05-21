package com.Z.NovelReaderKT.slider.Anims

fun interface ITipTransition {
    fun updateLocation(sliderViewY: Float, tipViewBottomY: Float)
}
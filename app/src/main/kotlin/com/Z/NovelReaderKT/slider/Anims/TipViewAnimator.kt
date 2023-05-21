package com.Z.NovelReaderKT.slider.Anims

import androidx.transition.Transition

fun interface TipViewAnimator {
    fun createTransition(): Transition?
}
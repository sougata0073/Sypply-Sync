package com.sougata.supplysync.util

import android.view.View
import androidx.navigation.NavOptions
import com.sougata.supplysync.R

class AnimationProvider(private val view: View) {

    var animationDuration: Long = 225
    private var prevScrollState = -1

    companion object {

        fun slideUpDownNavOptions() = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_bottom)
            .setExitAnim(R.anim.slide_out_bottom)
            .setPopEnterAnim(R.anim.slide_in_bottom)
            .setPopExitAnim(R.anim.slide_out_bottom)
            .build()

        fun slideRightLeftNavOptions() = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        fun popUpNavOptions() = NavOptions.Builder()
            .setEnterAnim(R.anim.popup_enter)
            .setExitAnim(R.anim.popup_exit)
            .setPopEnterAnim(R.anim.popup_enter_back)
            .setPopExitAnim(R.anim.popup_exit_back)
            .build()

    }

    fun slideDownFade() {
        this.view.animate()
            .alpha(0f)
            .translationY(this.view.height.toFloat())
            .setDuration(this.animationDuration)
            .withEndAction {
                this.view.visibility = View.GONE
            }.start()
    }

    fun slideDown(height: Float = this.view.height.toFloat()) {
        this.view.animate()
            .translationY(height)
            .setDuration(this.animationDuration)
            .withEndAction {
                this.view.visibility = View.GONE
            }.start()
    }

    fun slideUpFade() {
        this.view.visibility = View.VISIBLE
        this.view.alpha = 0f
        this.view.translationY = this.view.height.toFloat()

        this.view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(this.animationDuration)
            .start()
    }

    fun slideUp(height: Float = this.view.height.toFloat()) {
        this.view.visibility = View.VISIBLE
        this.view.translationY = height

        this.view.animate()
            .translationY(0f)
            .setDuration(this.animationDuration)
            .start()
    }

    fun slideDownFadeForScroll() {
        if (this.prevScrollState != 1) {
            this.slideDownFade()
        }
        this.prevScrollState = 1
    }

    fun slideUpFadeForScroll() {
        if (this.prevScrollState != -1) {
            this.slideUpFade()
        }
        this.prevScrollState = -1
    }

    fun collapseView(view: View) {
        view.animate()
            .translationY(view.height.toFloat())
            .setDuration(300)
            .start()
    }

    fun expandView(view: View) {
        view.animate()
            .translationY(0f)
            .setDuration(300)
            .start()
    }

}
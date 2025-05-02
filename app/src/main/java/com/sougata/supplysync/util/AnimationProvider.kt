package com.sougata.supplysync.util

import android.view.View
import androidx.navigation.NavOptions
import com.sougata.supplysync.R

class AnimationProvider(private val view: View) {

    var animationDuration: Long = 225

    /**
     * This flag ensures that animation happens only once per same scroll
     * Means if user scrolls down once and stops and then again scrolls down
     * then the animation will not happen because animation is already done during first down scroll
     * 1 -> User scrolled down now don't animate again for down scroll
     * -1 -> Same as above but from up scroll
     */
    private var prevScrollState = -1

    companion object {

        fun fragmentAnimationSlideUpDown() = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_bottom)
            .setExitAnim(R.anim.slide_out_bottom)
            .setPopEnterAnim(R.anim.slide_in_bottom)
            .setPopExitAnim(R.anim.slide_out_bottom)
            .build()

        fun fragmentAnimationSlideRightLeft() = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

    }

    fun slideDownFade() {
        /**
         * alpha(0) -> Tells the final alpha will be 0 when the animation finishes
         * translationY(this.view.height.toFloat()) -> Tells that moves the view down by its height in Y axis, it will not happen instantly
         * setDuration(200) -> Duration of the animation
         * withEndAction {this.view.visibility = View.GONE} -> When animation done make the view gone
         * start() -> Start the animation
         */
        this.view.animate()
            .alpha(0f)
            .translationY(this.view.height.toFloat())
            .setDuration(this.animationDuration)
            .withEndAction {
                this.view.visibility = View.GONE
            }.start()
    }

    fun slideUpFade() {

        /**
         * this.view.visibility = View.VISIBLE -> First make the view completely visible
         * this.view.alpha = 0f -> Set alpha 0f so the animation will start from the view faded and ends with original
         * this.view.translationY = this.view.height.toFloat() -> First place the view below its original position
         * alpha(1f) -> Tells the final alpha will be 1 when the animation finishes
         * translationY(0f) -> Tells that moves the view up to its original height in Y axis, it will not happen instantly
         */
        this.view.visibility = View.VISIBLE
        this.view.alpha = 0f
        this.view.translationY = this.view.height.toFloat()

        this.view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(this.animationDuration)
            .start()

    }

    fun slideDownFadeForScroll() {
        if (this.prevScrollState != 1) {
            this.slideDownFade()
//            Log.d("scroll", "up")
        }
        this.prevScrollState = 1
    }

    fun slideUpFadeForScroll() {
        if (this.prevScrollState != -1) {
            this.slideUpFade()
//            Log.d("scroll", "down")
        }
        this.prevScrollState = -1

    }

}
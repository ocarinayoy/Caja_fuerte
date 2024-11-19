package com.tdm.cajafuerte

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class SwipeGestureListener(
    context: Context,
    private val onSwipe: (String) -> Unit
) : GestureDetector.SimpleOnGestureListener() {

    private val gestureDetector = GestureDetector(context, this)

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 == null || e2 == null) return false

        val diffX = e2.x - e1.x
        val diffY = e2.y - e1.y

        return if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    onSwipe("right")
                } else {
                    onSwipe("left")
                }
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    fun onTouch(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }
}

package com.fxf.slide

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import kotlin.math.abs

class RightSlideLayout constructor(context : Context, attrs : AttributeSet? = null): RelativeLayout(context,attrs){
    private var mDownX // 手指按下的x轴位置
            = 0
    private var isSlideHorizontal = false // 正在水平拦截

    private var mDownY // 手指按下的y轴位置
            = 0
    private lateinit var mSlideContainerLayout: SlideContainerLayout

    fun setSlideContainerView(slideContainerLayout: SlideContainerLayout){
        mSlideContainerLayout = slideContainerLayout
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()

        val offsetX = x - mDownX
        if (!mSlideContainerLayout.isSlideShow){
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = x
                mDownY = y
                mSlideContainerLayout.setDownXY(mDownX,mDownY)
            }
            MotionEvent.ACTION_MOVE -> if (abs(x - mDownX) < abs(y - mDownY) && paddingLeft < x) {
                if (isSlideHorizontal) {
                    return mSlideContainerLayout.dispatchTouchEvent(event)
                }
            } else if ( offsetX < 0 && mSlideContainerLayout.isAlignLeftSide()) {
                return super.dispatchTouchEvent(event)
            } else if (abs(x - mDownX) > abs(y - mDownY)){
                isSlideHorizontal = true
                    return mSlideContainerLayout.dispatchTouchEvent(event)
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL ->{
                if (offsetX < 0 && mSlideContainerLayout.isAlignLeftSide()){
                    return super.dispatchTouchEvent(event)
                }
                if (abs(x - mDownX) > abs(y - mDownY) || isSlideHorizontal){
                    isSlideHorizontal = false
                    return mSlideContainerLayout.dispatchTouchEvent(event)
                }
                isSlideHorizontal = false
            }
        }
        return super.dispatchTouchEvent(event)
    }


}
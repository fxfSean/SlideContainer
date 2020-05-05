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
        // 获取坐标，这里用rawX 相对屏幕绝对位置，不然随手势移动过程中父布局的移动，导致获取的坐标左右抖动，会出现移动过程中左右一直抖动现象
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()

        val offsetX = x - mDownX
        if (!mSlideContainerLayout.isSlideShow){
            // Container滑块儿没滑出来不分发事件
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录按下点坐标
                mDownX = x
                mDownY = y
                mSlideContainerLayout.setDownXY(mDownX,mDownY)
            }
            MotionEvent.ACTION_MOVE -> if (abs(x - mDownX) < abs(y - mDownY) && paddingLeft < x) {
                if (isSlideHorizontal) {// 上下滑动情况处理
                    return mSlideContainerLayout.dispatchTouchEvent(event)
                }
            } else if ( offsetX < 0 && mSlideContainerLayout.isAlignLeftSide()) {
                // 向左滑动，滑块儿已经靠最左边了，不分发
                return super.dispatchTouchEvent(event)
            } else if (abs(x - mDownX) > abs(y - mDownY)){
                isSlideHorizontal = true
                    return mSlideContainerLayout.dispatchTouchEvent(event)
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL ->{
                // 水平方向移动，分发事件
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
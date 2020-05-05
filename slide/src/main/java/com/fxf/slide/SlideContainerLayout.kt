package com.fxf.slide

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import java.util.*
import kotlin.math.abs


class SlideContainerLayout constructor(context: Context, attrs: AttributeSet? = null)
    : RelativeLayout(context, attrs),View.OnClickListener {

    private var mDownX // 手指按下的x轴位置
            = 0
    private var mDownY // 手指按下的y轴位置
            = 0
    private var startX // 滑动开始时x轴偏移量
            = 0
    private var translateX // 当前x轴偏移量
            = 0
    private var endX // 动画结束时x轴偏移量
            = 0
    private var mLastOffsetList // 上次右滑位移
            = LinkedList<Int>()
    private var isCleared // 是否已清屏
            = false
    var isSlideShow // 是否已滑入滑块
            = false
    private var isSliderGoning // 是否正在滑出屏幕
            = false
    private var isSlideVertical //是否竖直滑动
            = false
    private var mVelocityTracker // 计算滑动速度
            : VelocityTracker? = null
    private lateinit var mClearAnimator // 动画
            : ValueAnimator
    private lateinit var mSlideInAnimator // 动画
            : ValueAnimator

    private val MASK_DARK_COLOR = 0xAA // 灰色蒙层
    private val MASK_TRANSPANT_COLOR = 0x00000000 // 透明

    private lateinit var mBgColorView: View

    private val mClearViews = ArrayList<View>() // 需要清除的View

    private var mSlideView: RightSlideLayout? = null //需要滑入的view

    init {
        initView()
    }

    private fun initView() {
        mBgColorView = View(context)
        mBgColorView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mBgColorView.isClickable = true
        addView(mBgColorView, childCount - 4) // 放在WatchLiveLayout下层

        mBgColorView.setOnClickListener {
            if (isSlideShow && !mSlideInAnimator.isRunning && !isSlideVertical) {
                sliderGoneWithAnim()
            }
        }

        mVelocityTracker = VelocityTracker.obtain()

        mClearAnimator = ValueAnimator.ofFloat(0f, 1.0f).setDuration(300)
        mClearAnimator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            translateClearChild((startX + value * (endX - startX)).toInt())
        })
        mClearAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isCleared = !isCleared
            }
        })
        mSlideInAnimator = ValueAnimator.ofFloat(0f, 1.0f).setDuration(500)
        mSlideInAnimator.interpolator = DecelerateInterpolator(3f)
        mSlideInAnimator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            translateSlideView((startX + value * (endX - startX)).toInt())
        })
        mSlideInAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                mSlideView!!.visibility = View.VISIBLE
                mBgColorView.isClickable = true
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!isSlideShow && translateX == 0) {
                    isSlideShow = !isSlideShow
                } else if (isSlideShow && abs(translateX) == width - mSlideView!!.paddingLeft) {
                    isSlideShow = !isSlideShow
                }
                if (!isSlideShow) {
                    parent.requestDisallowInterceptTouchEvent(false)
                    mSlideView!!.visibility = View.GONE
                    removeView(mBgColorView)
                    addView(mBgColorView, childCount - 4)
                }
                isSliderGoning = false
            }
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        if (mClearAnimator.isRunning || mSlideInAnimator.isRunning || isSlideShow) {
            // 滑入情况下，禁止上下滑切换直播间
            parent.requestDisallowInterceptTouchEvent(true)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = x
                mDownY = y
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
                isSlideVertical = abs(y - mDownY) > 5
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        if (mClearAnimator.isRunning || mSlideInAnimator.isRunning || isSlideShow) {
            // 滑入情况下，禁止上下滑切换直播间
            parent.requestDisallowInterceptTouchEvent(true)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = x
                mDownY = y
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mClearAnimator.isRunning && mDownY > 200 && abs(x - mDownX) > abs(y - mDownY)) {
                    // 清屏不在执行时 && 高度大于200dp（解决进入房间头像滑动冲突）&& 横向滑动时拦截事件
                    if (abs(x - mDownX) > 10) {
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                isSlideVertical = abs(y - mDownY) > 5
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mVelocityTracker!!.addMovement(event)
        val x = event.rawX.toInt()
        val offsetX = x - mDownX
        if (mLastOffsetList.size > 2){
            mLastOffsetList.removeFirst()
        }
        mLastOffsetList.add(offsetX)
        var slideRight = (offsetX - mLastOffsetList.first) > 0
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if ((isSlideShow) && offsetX > 0 && mSlideInAnimator.isRunning && !isSliderGoning) {
                    // 滑入情况下，向右滑一段松开，再向右滑，清除回弹动画，跟随手势
                    mSlideInAnimator.cancel()
                    translateSlideView(offsetX)
                }
                if ((isSlideShow) && offsetX > 0 && !mSlideInAnimator.isRunning) {
                    // 滑入情况下，向右滑，跟随手势
                    translateSlideView(offsetX)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                mVelocityTracker!!.computeCurrentVelocity(10)
                if (isSlideShow && offsetX > 0 && abs(offsetX) > width / 3 && !isSliderGoning && mVelocityTracker!!.xVelocity >= 0) {
                    // 滑入情况下，向右滑距离超过宽度1/3，滑出滑块
                    startX = offsetX
                    endX = width - mSlideView!!.paddingLeft
                    isSliderGoning = true
                    mSlideInAnimator.start()
                    return true
                }
                if (abs(mVelocityTracker!!.xVelocity) > 1) {
                    if (isCleared && offsetX < 0) {
                        // 清屏情况下，左滑速度超过10个像素时 ===》滑入清屏控件
                        layerShowWithAnim()
                    } else if (!isCleared && offsetX > 0 && !isSlideShow && !mSlideInAnimator.isRunning) {
                        // 未清屏 && 向右速度 > 10 && 没滑入滑块 && 滑块动画没执行的时候 ===》清屏
                        layerGoneWithAnim()
                    } else if (isSlideShow && offsetX > 0 && slideRight) {
                        // 滑入情况下 && 向右速度 > 10 ===》滑出滑块
                        mSlideInAnimator.cancel()
                        isSliderGoning = true
                        startX = translateX
                        endX = width - mSlideView!!.paddingLeft
                        mSlideInAnimator.start()
                    } else if (isSlideShow && offsetX < 0 && translateX != 0) {
                        // 滑入情况下 && 向左速度 > 10 && 已经向右滑动了一段距离 ===》 滑块回弹
                        startX = translateX
                        endX = 0
                        mSlideInAnimator.start()
                    } else if (!isSlideShow && offsetX < 0 && !mSlideInAnimator.isRunning) {
                        // 没滑入情况下 && 向左滑速度 > 10 && 没右正在滑入情况下 ===》 滑入滑块
                        sliderShowWithAnim()
                    } else {
                        if (isSlideShow && translateX != 0) {
                            // 滑入情况下 && 已经向右滑动过，速度没达到松开 ===》回弹
                            startX = translateX
                            mSlideInAnimator.start()
                        }
                    }
                }else {
                    if (isSlideShow && translateX != 0) {
                        // 滑入情况下 && 已经向右滑动过，速度没达到松开 ===》回弹
                        startX = translateX
                        mSlideInAnimator.start()
                    }
                }
                return super.onTouchEvent(event)
            }
            MotionEvent.ACTION_CANCEL -> {
                if (isSlideShow) {
                    //取消事件时，滑入情况下回弹
                    startX = translateX
                    mSlideInAnimator.start()
                }
            }
        }
        return super.onTouchEvent(event)
    }



    /**
     * 添加需要清屏的view
     */
    fun addClearViews(vararg views: View?) {
        for (cell in views) {
            if (!mClearViews.contains(cell)) {
                mClearViews.add(cell!!)
            }
        }
    }

    /**
     * 添加需要滑入的view
     */
    fun addSlideView(view: RightSlideLayout) {
        mSlideView = view
        mSlideView!!.setSlideContainerView(this)
    }

    private fun layerShowWithAnim() {
        startX = width
        endX = 0
        mClearAnimator.start()
    }

    private fun layerGoneWithAnim() {
        startX = 0
        endX = width
        mClearAnimator.start()
    }

    fun sliderGone() {
        mSlideView?.visibility = View.GONE
        isSlideShow = false
        mBgColorView.setBackgroundColor(MASK_TRANSPANT_COLOR)
        removeView(mBgColorView)
        addView(mBgColorView, childCount - 4)
    }

    private fun sliderGoneWithAnim() {
        // 滑入情况下，点击空白处滑出滑块
        isSliderGoning = true
        startX = translateX
        endX = width - mSlideView!!.paddingLeft
        mSlideInAnimator.start()
    }

    private fun sliderShowWithAnim() {
        if (mSlideInAnimator.isRunning || mClearAnimator.isRunning)
            return
        removeView(mBgColorView)
        addView(mBgColorView, childCount - 1)
        startX = measuredWidth - mSlideView!!.paddingLeft
        translateX = measuredWidth - mSlideView!!.paddingLeft
        endX = 0
        mSlideInAnimator.start()
    }

    /**
     * 移动清屏控件
     */
    private fun translateClearChild(translate: Int) {
        for (i in mClearViews.indices) {
            mClearViews[i].translationX = translate.toFloat()
        }
    }

    fun isAlignLeftSide(): Boolean{
        return isSlideShow && translateX == 0
    }

    /**
     * 移动滑块儿
     */
    private fun translateSlideView(translate: Int) {
        val percent = (mSlideView!!.width.toFloat() - translate) / mSlideView!!.width
        val color = (MASK_DARK_COLOR * percent).toInt() shl 24
        mBgColorView.setBackgroundColor(color)
        translateX = translate
        mSlideView!!.translationX = translate.toFloat()
    }

    fun reset() {
        mClearAnimator.cancel()
        mSlideInAnimator.cancel()
        if (isCleared)
            translateClearChild(0)
        mSlideView!!.visibility = View.GONE
        translateX = 0
        isSliderGoning = false
        isSlideShow = false
        isCleared = false
    }

    fun release() {
        mVelocityTracker?.recycle()
    }

    fun setDownXY(downX: Int, downY: Int) {
        mDownX = downX
        mDownY = downY
    }

    override fun onClick(p0: View?) {

    }
}
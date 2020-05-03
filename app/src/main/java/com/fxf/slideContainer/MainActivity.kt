package com.fxf.slideContainer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fxf.slide.RightSlideLayout
import com.fxf.slide.SlideContainerLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private lateinit var mRightSlideLayout: RightSlideLayout

    private lateinit var mSlideContainerLayout: SlideContainerLayout

    private lateinit var mListFragment: ListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSlideContainerLayout = layout_slider_container

        mRightSlideLayout = layout_right_slider

        mSlideContainerLayout.addClearViews(tvHello,ll12,tv333)
        mSlideContainerLayout.addSlideView(mRightSlideLayout)

        mListFragment = ListFragment()
        supportFragmentManager.beginTransaction().replace(R.id.list_fragment,mListFragment).commit()
    }
}

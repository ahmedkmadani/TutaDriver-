package com.tutaap.tutadriver

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_welcome.view.*

public class WelcomeActivity : AppCompatActivity() {

    private var introViewPager: ViewPager? = null
    private var introViewPagerAdapter: IntroScreenViewPagerAdapter? = null
    private var introBullets: Array<TextView>? = null
    private var introBulletsLayout: LinearLayout? = null
    private var introSliderLayouts: IntArray? = null
    private var btnSkip: Button? = null
    var btnNext: Button? = null
    private var prefManager: PrefManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val welcomeLayout = layoutInflater.inflate(R.layout.activity_welcome, null)
        setContentView(welcomeLayout)

        prefManager = PrefManager(this)
        if (!prefManager!!.isFirstTimeLaunch) {
            applicationStartup()
            finish()
        }

        introViewPager = welcomeLayout.view_pager
        introBulletsLayout = welcomeLayout.layoutDots
        btnSkip = welcomeLayout.btn_skip
        btnNext = welcomeLayout.btn_next

        introSliderLayouts = intArrayOf(
            R.layout.welcome_slide1,
            R.layout.welcome_slide2,
            R.layout.welcome_slide3,
            R.layout.welcome_slide4)

        makeIIntroBullets(0)
        introViewPagerAdapter = IntroScreenViewPagerAdapter()
        introViewPager!!.adapter = introViewPagerAdapter
        introViewPager!!.addOnPageChangeListener(introViewPagerListener)
        (btnSkip as View?)!!.setOnClickListener { applicationStartup() }
        (btnNext as View?)!!.setOnClickListener {

            val current = getItem(+1)
            if (current < introSliderLayouts!!.size) {
                introViewPager!!.currentItem = current
            } else {
                applicationStartup()
            }
        }

    }

    private fun makeIIntroBullets(currentPage: Int) {
        var arraySize = introSliderLayouts!!.size
        introBullets = Array(arraySize) { textboxInit() }
        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)
        introBulletsLayout!!.removeAllViews()
        for (i in 0 until introBullets!!.size) {
            introBullets!![i] = TextView(this)
            introBullets!![i].text = Html.fromHtml("&#9679;")
            introBullets!![i].textSize = 15f
            introBullets!![i].setTextColor(colorsInactive[currentPage])
            introBulletsLayout!!.addView(introBullets!![i])
        }
        if (introBullets!!.isNotEmpty())
            introBullets!![currentPage].setTextColor(colorsActive[currentPage])
    }

    private fun textboxInit(): TextView {
        return TextView(applicationContext)
    }

    private fun getItem(i: Int): Int {
        return introViewPager!!.currentItem + i
    }

    private fun applicationStartup() {
        prefManager!!.isFirstTimeLaunch = false
        startActivity(Intent(this@WelcomeActivity, SigninActivity::class.java))
        finish()
    }

    private var introViewPagerListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            makeIIntroBullets(position)

            if (position == introSliderLayouts!!.size - 1) {
                btnNext!!.text = getString(R.string.start)
                btnSkip!!.visibility = View.GONE
            } else {
                btnNext!!.text = getString(R.string.next)
                btnSkip!!.visibility = View.VISIBLE
            }
        }
        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
            //Do nothing for now
        }
        override fun onPageScrollStateChanged(arg0: Int) {
            //Do nothing for now
        }
    }


    inner class IntroScreenViewPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater: LayoutInflater = LayoutInflater.from(applicationContext)
            val view = layoutInflater.inflate(introSliderLayouts!![position], container, false)
            container.addView(view)
            return view
        }
        override fun getCount(): Int {
            return introSliderLayouts!!.size
        }
        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }
}
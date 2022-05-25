package com.github.vkay94.doubletapplayerviewexample.rcti

import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.vkay94.doubletapplayerviewexample.DataAndUtils
import com.github.vkay94.doubletapplayerviewexample.R
import com.github.vkay94.doubletapplayerviewexample.TextViewStyler
import com.github.vkay94.doubletapplayerviewexample.fragments.PageViewModel
import com.github.vkay94.doubletapplayerviewexample.fragments.SecondsViewFragment
import com.github.vkay94.doubletapplayerviewexample.fragments.SectionsPagerAdapter
import com.github.vkay94.doubletapplayerviewexample.fragments.ShapeFragment
import com.github.vkay94.doubletapplayerviewexample.openInBrowser
import com.github.vkay94.dtpv.PlayerDoubleTapListener
import com.github.vkay94.dtpv.rcti.DoubleTapOverlay
import kotlinx.android.synthetic.main.activity_main_rcti.*
import kotlinx.android.synthetic.main.exo_playback_control_view_yt.*

class MainActivityRcti : BaseVideoActivityRcti() {

    private var isVideoFullscreen = false
    private var currentVideoId = -1
    private var lastClickTime: Long = 0
    val DOUBLE_CLICK_TIME_DELTA: Long = 650

    private lateinit var viewModel: PageViewModel

    private var listener:PlayerDoubleTapListener?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_rcti)
        setSupportActionBar(toolbar)

        this.videoPlayer = previewPlayerView
        initDoubleTapPlayerView()
        initViewModel()
        startNextVideo()

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        sectionsPagerAdapter.addFragment(ShapeFragment.newInstance())
        sectionsPagerAdapter.addFragment(SecondsViewFragment.newInstance())

        view_pager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(view_pager)

        fullscreen_button.setOnClickListener {
            toggleFullscreen()
        }

        listener = ytOverlay.playerDoubleTapListener

        left.setOnClickListener {
            Log.d("TAG", "left: ")
            listener?.onDoubleTapStarted(it.x,it.y)

            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                lastClickTime = 0
                listener?.onDoubleTapProgressUp(it.x,it.y)
            }
            lastClickTime = clickTime

        }

        right.setOnClickListener {
            Log.d("TAG", "right: ")
            listener?.onDoubleTapStarted(it.x*2,it.y)

            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                lastClickTime = 0
                listener?.onDoubleTapProgressUp(it.x*2,it.y)
            }
            lastClickTime = clickTime
        }

    }

    private fun initDoubleTapPlayerView() {
        ytOverlay
            // Uncomment this line if the DoubleTapPlayerView is not set via XML
            //.playerView(previewPlayerView)
            .performListener(object :DoubleTapOverlay.PerformListener{
                override fun onAnimationStart() {
                    previewPlayerView.useController = false
                    ytOverlay.visibility = View.VISIBLE
                }
                override fun onAnimationEnd() {
                    ytOverlay.visibility = View.GONE
                    previewPlayerView.useController = true
                }
            })

//        previewPlayerView.doubleTapDelay = 800
        // Uncomment this line if the PlayerDoubleTapListener is not set via XML
        // previewPlayerView.controller(ytOverlay)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            tapCircleColor.value = ytOverlay.tapCircleColor
            arcSize.value = DataAndUtils.pxToDp(this@MainActivityRcti, ytOverlay.arcSize)
            circleBackgroundColor.value = ytOverlay.circleBackgroundColor
            circleExpandDuration.value = ytOverlay.animationDuration
            fontSize.value = DataAndUtils.pxToSp(this@MainActivityRcti, ytOverlay.secondsTextView.textSize)
            typeFace.value = Typeface.NORMAL
            iconSpeed.value = ytOverlay.iconAnimationDuration
        }

        viewModel.circleExpandDuration.observe(this, Observer {
            ytOverlay.animationDuration(it)
        })
        viewModel.arcSize.observe(this, Observer {
            ytOverlay.arcSize(DataAndUtils.dpToPx(this@MainActivityRcti, it.toFloat()))
        })
        viewModel.fontSize.observe(this, Observer {
            TextViewStyler().textSize(it).applyTo(ytOverlay.secondsTextView)
        })
        viewModel.typeFace.observe(this, Observer {
            TextViewStyler().textStyle(it).applyTo(ytOverlay.secondsTextView)
        })
        viewModel.tapCircleColor.observe(this, Observer {
            ytOverlay.tapCircleColorInt(it)
        })
        viewModel.circleBackgroundColor.observe(this, Observer {
            ytOverlay.circleBackgroundColorInt(it)
        })
        viewModel.secondsIcon.observe(this, Observer {
            ytOverlay.icon(it)
        })
        viewModel.iconSpeed.observe(this, Observer {
            ytOverlay.iconAnimationDuration(it)
        })
    }

    private fun startNextVideo() {
        releasePlayer()
        initializePlayer()
        ytOverlay.player(player!!)

        currentVideoId = (currentVideoId + 1).rem(DataAndUtils.videoList.size)
        buildMediaSource(Uri.parse(DataAndUtils.videoList[currentVideoId]))
    }

    private fun toggleFullscreen() {
        if (isVideoFullscreen) {
            setFullscreen(false)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;
            if(supportActionBar != null){
                supportActionBar?.show();
            }
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            isVideoFullscreen = false
        } else {
            setFullscreen(true)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

            if(supportActionBar != null){
                supportActionBar?.hide();
            }
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            isVideoFullscreen = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_main_action_github -> {
                openInBrowser(DataAndUtils.GITHUB_LINK)
                true
            }
            R.id.menu_main_action_change_video -> {
                startNextVideo()
                Toast.makeText(this, "Video has changed", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onBackPressed() {
        if (isVideoFullscreen) {
            toggleFullscreen()
            return
        }
        super.onBackPressed()
    }
}
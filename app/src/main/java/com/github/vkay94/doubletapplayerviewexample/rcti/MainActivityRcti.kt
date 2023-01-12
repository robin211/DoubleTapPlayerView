package com.github.vkay94.doubletapplayerviewexample.rcti

import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
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
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.android.synthetic.main.activity_main_rcti.*
import kotlinx.android.synthetic.main.exo_playback_control_view_yt.*

class MainActivityRcti : BaseVideoActivityRcti() {

    private var isVideoFullscreen = false
    private var currentVideoId = -1
    private var lastClickTime: Long = 0
    val DOUBLE_CLICK_TIME_DELTA: Long = 650
    private var portraitParams : ViewGroup.LayoutParams? = null

    private lateinit var viewModel: PageViewModel

    private var listener:PlayerDoubleTapListener?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_rcti)
        setSupportActionBar(toolbar)

        this.videoPlayer = previewPlayerView
        videoPlayer!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
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

        exo_play.setOnClickListener {
            player?.play()
        }

        exo_pause.setOnClickListener {
            player?.pause()
        }

        listener = ytOverlay.playerDoubleTapListener

        val gestureListener = DoubleTapGestureListener(this.previewPlayerView)
        val gestureDetector = GestureDetectorCompat(this, gestureListener)


        val scaleGestureDetector = ScaleGestureDetector(
            this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    if (isVideoFullscreen){
                        if (detector.scaleFactor > 1){
                            previewPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                            player?.videoScalingMode=
                                C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                            Log.d("SCALE", "PAN")
                        }else{
                            previewPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            player?.videoScalingMode=
                                C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                            Log.d("SCALE", "PINCH")
                        }
                    }
                    return super.onScale(detector)
                }
            }
        )

        mid.setOnTouchListener{
                _, event ->
            gestureDetector.onTouchEvent(event)
            scaleGestureDetector.onTouchEvent(event)
        }

        left.setOnClickListener {
            listener?.onDoubleTapStarted(it.x,it.y)
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                lastClickTime = 0
                listener?.onDoubleTapProgressUp(it.x,it.y)
            }else{
                Log.d("TAG", "left: ")
            }
            lastClickTime = clickTime

        }

        right.setOnClickListener {
            listener?.onDoubleTapStarted(it.x*2,it.y)
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                lastClickTime = 0
                listener?.onDoubleTapProgressUp(it.x*2,it.y)
            }else{
                Log.d("TAG", "right: ")
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

    private fun toggleFullscreen() = if (isVideoFullscreen) {
        setFullscreen(false)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;
        if(supportActionBar != null){
            supportActionBar?.show();
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        frameLayout.layoutParams = portraitParams
        isVideoFullscreen = false
    } else {
        setFullscreen(true)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        if(supportActionBar != null){
            supportActionBar?.hide();
        }
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height: Int = displayMetrics.heightPixels
        val width: Int = displayMetrics.widthPixels
        portraitParams = frameLayout.layoutParams
        frameLayout.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,width)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        isVideoFullscreen = true
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

    private class DoubleTapGestureListener(private val rootView: View) : GestureDetector.SimpleOnGestureListener() {

        private val mHandler = Handler()
        private val mRunnable = Runnable {
            if (DEBUG) Log.d(TAG, "Runnable called")
            isDoubleTapping = false
            controls?.onDoubleTapFinished()
        }

        var controls: PlayerDoubleTapListener? = null
        var isDoubleTapping = false
        var doubleTapDelay: Long = 650

        /**
         * Resets the timeout to keep in double tap mode.
         *
         * Called once in [PlayerDoubleTapListener.onDoubleTapStarted]. Needs to be called
         * from outside if the double tap is customized / overridden to detect ongoing taps
         */
        fun keepInDoubleTapMode() {
            isDoubleTapping = true
            mHandler.removeCallbacks(mRunnable)
            mHandler.postDelayed(mRunnable, doubleTapDelay)
        }

        /**
         * Cancels double tap mode instantly by calling [PlayerDoubleTapListener.onDoubleTapFinished]
         */
        fun cancelInDoubleTapMode() {
            mHandler.removeCallbacks(mRunnable)
            isDoubleTapping = false
            controls?.onDoubleTapFinished()
        }

        override fun onDown(e: MotionEvent): Boolean {
            // Used to override the other methods
            if (isDoubleTapping) {
                controls?.onDoubleTapProgressDown(e.x, e.y)
                return true
            }
            return super.onDown(e)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (isDoubleTapping) {
                if (DEBUG) Log.d(TAG, "onSingleTapUp: isDoubleTapping = true")
                controls?.onDoubleTapProgressUp(e.x, e.y)
                return true
            }
            return super.onSingleTapUp(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // Ignore this event if double tapping is still active
            // Return true needed because this method is also called if you tap e.g. three times
            // in a row, therefore the controller would appear since the original behavior is
            // to hide and show on single tap
            if (isDoubleTapping) return true
            if (DEBUG) Log.d(TAG, "onSingleTapConfirmed: isDoubleTap = false")
            return rootView.performClick()
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // First tap (ACTION_DOWN) of both taps
            if (DEBUG) Log.d(TAG, "onDoubleTap")
            if (!isDoubleTapping) {
                isDoubleTapping = true
                keepInDoubleTapMode()
                controls?.onDoubleTapStarted(e.x, e.y)
            }
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            // Second tap (ACTION_UP) of both taps
            if (e.actionMasked == MotionEvent.ACTION_UP && isDoubleTapping) {
                if (DEBUG) Log.d(
                    TAG,
                    "onDoubleTapEvent, ACTION_UP"
                )
                controls?.onDoubleTapProgressUp(e.x, e.y)
                return true
            }
            return super.onDoubleTapEvent(e)
        }

        companion object {
            private const val TAG = ".DTGListener"
            private var DEBUG = true
        }
    }
}
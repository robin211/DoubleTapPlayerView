package com.github.vkay94.doubletapplayerviewexample.rcti

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.github.vkay94.doubletapplayerviewexample.Mp4ExtractorFactory
import com.github.vkay94.doubletapplayerviewexample.R
import com.github.vkay94.dtpv.DoubleTapPlayerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main_rcti.*
import kotlinx.android.synthetic.main.activity_main_rcti.view.*


@SuppressLint("Registered")
open class BaseVideoActivityRcti : AppCompatActivity() {

    var videoPlayer: PlayerView? = null
    var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }

    fun buildMediaSource(mUri: Uri) {
        val dataSourceFactory = DefaultDataSourceFactory(
            this@BaseVideoActivityRcti,
            Util.getUserAgent(this@BaseVideoActivityRcti, resources.getString(R.string.app_name)),
            DefaultBandwidthMeter.Builder(this@BaseVideoActivityRcti).build()
        )
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory, Mp4ExtractorFactory())
            .createMediaSource(MediaItem.fromUri(mUri))

        player?.prepare(videoSource)
        player?.playWhenReady = true
    }

    fun initializePlayer() {
        if (player == null) {
            val loadControl: LoadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    MIN_BUFFER_DURATION,
                    MAX_BUFFER_DURATION,
                    MIN_PLAYBACK_START_BUFFER,
                    MIN_PLAYBACK_RESUME_BUFFER
                )
                .createDefaultLoadControl()

            player = ExoPlayer.Builder(this)
                .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                .setLoadControl(loadControl)
                .build()
            videoPlayer?.player = player
        }
    }

    // Player Lifecycle
    fun releasePlayer() {
        if (player != null) {
            player?.release()
            player = null
        }
    }

    fun pausePlayer() {
        if (player != null) {
            player?.playWhenReady = false
            player?.playbackState
        }
    }

    fun resumePlayer() {
        if (player != null) {
            player?.playWhenReady = true
            player?.playbackState
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onRestart() {
        super.onRestart()
        if (player?.playbackState == Player.STATE_READY && player?.playWhenReady!!)
            resumePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (player != null) {
            player?.release()
            player = null
        }
    }

    fun setFullscreen(fullscreen: Boolean) {
        if (fullscreen) {
            this.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            previewPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player?.videoScalingMode=
                C.VIDEO_SCALING_MODE_SCALE_TO_FIT


        } else {
            this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    companion object {
        const val MIN_BUFFER_DURATION = 15000
        const val MAX_BUFFER_DURATION = 60000
        const val MIN_PLAYBACK_START_BUFFER = 2500
        const val MIN_PLAYBACK_RESUME_BUFFER = 5000

        fun <T: BaseVideoActivityRcti> newIntent(context: Context, activity: Class<T>): Intent =
            Intent(context, activity)
    }
}

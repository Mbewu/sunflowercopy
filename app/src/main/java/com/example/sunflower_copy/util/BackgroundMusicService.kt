package com.example.sunflower_copy.util

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.sunflower_copy.R
import timber.log.Timber
import kotlin.math.ln

fun getSongResource(songResourceString: String): Int {

    return when(songResourceString) {
        "australia_1" -> R.raw.peaceful_music_australia_1
        "australia_2" -> R.raw.peaceful_music_australia_2
        "australia_3" -> R.raw.peaceful_music_australia_3
        "australia_4" -> R.raw.peaceful_music_australia_4
        "australia_5" -> R.raw.peaceful_music_australia_5
        else -> R.raw.peaceful_music_australia_1
    }
}

class BackgroundMusicService : Service() {
    var mediaPlayer: MediaPlayer? = null
    var audioManager: AudioManager? = null
    private var volume = 1f // log scaled
    var maxVolume = 100 // 0 - 100 normal scale
    var length = 0
    private val defaultSong = R.raw.peaceful_music_australia_1
    var currentSong: Int? = null

    var binder: IBinder = LocalBinder()

    override fun onBind(arg0: Intent?): IBinder? {
        return binder
    }

    fun onUnBind(arg0: Intent?): IBinder? {
        return null
    }

    inner class LocalBinder : Binder() {
        fun getService(): BackgroundMusicService = this@BackgroundMusicService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("starting BackgroundMusicService service")
        //startMusic(defaultSong)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        Timber.i("creating BackgroundMusicService service")
        super.onCreate()
    }

    private fun startMusic(songResource: Int) {
        mediaPlayer = MediaPlayer.create(
            this,
            songResource
        )
        //mediaPlayer!!.setOnErrorListener(this)
        if (mediaPlayer!= null) {
            mediaPlayer!!.setLooping(true)
            mediaPlayer!!.setVolume(volume, volume)
            currentSong = songResource
        }

        mediaPlayer!!.start()
    }

    fun changeMusic(songResourceString: String) {

        // map string to song resource
        val songResource = getSongResource(songResourceString)
        if(songResource != currentSong) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            startMusic(songResource)
        } else {
            mediaPlayer!!.start()
        }
    }

    fun changeVolume(inputVolume: Int = 100) {
        // limit volume to maxVolume - 1
        var volumeNormal = inputVolume
        if(volumeNormal == maxVolume) {
            volumeNormal--
        }
        val logVolume = 1f - ln((maxVolume - volumeNormal).toDouble()) / ln(maxVolume.toDouble())
        volume = logVolume.toFloat()
        Timber.i("setting volume to: ".plus(volume))
        mediaPlayer?.setVolume(volume, volume)
    }

    fun pauseMusic() {
        if(mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying()) {
                mediaPlayer!!.pause()
                length = mediaPlayer!!.getCurrentPosition()
            }
        }
    }

    fun resumeMusic() {
        if(mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying() === false) {
                mediaPlayer!!.seekTo(length)
                mediaPlayer!!.start()
            }
        } else {
            startMusic(defaultSong)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        mediaPlayer = null
    }
}
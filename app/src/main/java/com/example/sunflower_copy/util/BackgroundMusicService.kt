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
import kotlin.properties.Delegates

fun getSongResource(songResourceString: String): Int {

    return when(songResourceString) {
        "peacuful_music_australia_1" -> R.raw.peaceful_music_australia_1
        "peacuful_music_australia_2" -> R.raw.peaceful_music_australia_2
        "peacuful_music_australia_3" -> R.raw.peaceful_music_australia_3
        "peacuful_music_australia_4" -> R.raw.peaceful_music_australia_4
        "peacuful_music_australia_5" -> R.raw.peaceful_music_australia_5
        else -> R.raw.peaceful_music_australia_1
    }
}

class BackgroundMusicService : Service() {
    var mediaPlayer: MediaPlayer? = null
    var audioManager: AudioManager? = null
    private var volume = 1f // log scaled
    private val maxVolume = 100 // 0 - 100 normal scale
    private var length = 0
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
        createMusicPlayer(defaultSong)  // we want to create the music player so it's there
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        Timber.i("creating BackgroundMusicService service")
        super.onCreate()
    }

    private fun createMusicPlayer(songResource: Int) {

        Timber.i("hi songResource = $songResource")
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
    }

    // we expect only one of these, assume string first
    fun changeMusic(songResourceString: String = "", songResourceInt: Int = -1) {


        var songResource = defaultSong
        // map string to song resource
        if(songResourceString != "") {
            //songResource = getSongResource(songResourceString)
            songResource = application.resources.getIdentifier(songResourceString,"raw",application.packageName)
            Timber.i("songResource = $songResource")
        } else if (songResourceInt != -1) {
            songResource = songResourceInt
        }

        if (mediaPlayer!= null) {
            if (songResource != currentSong) {
                nullifyMediaPlayer()
                createMusicPlayer(songResource)
                mediaPlayer!!.start()
            } else {
                mediaPlayer!!.start()
            }
        } else {
            createMusicPlayer(songResource)
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
        Timber.i("setting volume to: $volume")
        mediaPlayer?.setVolume(volume, volume)
    }

    fun pauseMusic() {
        if(mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                length = mediaPlayer!!.getCurrentPosition()
            }
        }
    }

    fun resumeMusic() {
        if(mediaPlayer != null) {
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.seekTo(length)
                mediaPlayer!!.start()
            }
        }
    }


    fun startMusic() {
        if(mediaPlayer != null) {
            resumeMusic()
        } else {
            createMusicPlayer(defaultSong)
            mediaPlayer!!.start()
        }
    }

    fun stopMusic() {
        nullifyMediaPlayer()
    }


    private fun nullifyMediaPlayer() {
        mediaPlayer?.apply { stop() }?.apply { release() }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        nullifyMediaPlayer()
    }
}
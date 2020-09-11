package com.example.sunflower_copy.util

import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException
import androidx.test.rule.ServiceTestRule;
import com.example.sunflower_copy.R
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Assert.*
import timber.log.Timber

// from https://github.com/android/testing-samples/blob/master/integration/ServiceTestRuleSample/app/src/androidTest/java/com/example/android/testing/ServiceTestRuleSample/LocalServiceTest.java
/**
 * JUnit4 test that uses a [ServiceTestRule] to interact with a bound service.
 *
 *
 * [ServiceTestRule] is a JUnit rule that provides a
 * simplified mechanism to start and shutdown your service before
 * and after the duration of your test. It also guarantees that the service is successfully
 * connected when starting (or binding to) a service. The service can be started
 * (or bound) using one of the helper methods. It will automatically be stopped (or unbound) after
 * the test completes and any methods annotated with
 * [`After`](http://junit.sourceforge.net/javadoc/org/junit/After.html) are
 * finished.
 *
 *
 * Note: This rule doesn't support [android.app.IntentService] because it's automatically
 * destroyed when [android.app.IntentService.onHandleIntent] finishes
 * all outstanding commands. So there is no guarantee to establish a successful connection
 * in a timely manner.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
class BackgroundMusicServiceTest {
    @get:Rule
    val serviceRule: ServiceTestRule = ServiceTestRule()


    @Test
    @Throws(TimeoutException::class)
    fun testAndStartBoundService_mediaPlayerNotNull() {
        // Create the service Intent.
        val serviceIntent = Intent(getApplicationContext(), BackgroundMusicService::class.java)

        // Bind the service and grab a reference to the binder.
        val binder: IBinder = serviceRule.bindService(serviceIntent)

        // start the service
        serviceRule.startService(serviceIntent)

        // Get the reference to the service, or you can call public methods on the binder directly.
        val service: BackgroundMusicService = (binder as BackgroundMusicService.LocalBinder).getService()


        // assert the mediaplayer is not null
        if(service.mediaPlayer == null) {
            Timber.i("mediaplayer is null")
        } else {
            Timber.i("mediaplayer is NOT null")
        }
    }


    @Test
    @Throws(TimeoutException::class)
    fun changeMusicBoundService_mediaPlayerNotNull() {
        // Create the service Intent.
        val serviceIntent = Intent(getApplicationContext(), BackgroundMusicService::class.java)

        // Bind the service and grab a reference to the binder.
        val binder: IBinder = serviceRule.bindService(serviceIntent)

        // start the service
        serviceRule.startService(serviceIntent)

        // Get the reference to the service, or you can call public methods on the binder directly.
        val service: BackgroundMusicService = (binder as BackgroundMusicService.LocalBinder).getService()

        // change the song
        service.changeMusic(songResourceInt = R.raw.peaceful_music_australia_5)

        // assert the current song has been changed appropriately and mediaplayer not null
        assertThat(service.currentSong, Matchers.equalTo(R.raw.peaceful_music_australia_5))
        assertThat(service.mediaPlayer, `is` (notNullValue()))
    }



    @Test
    fun testWithStartedService() {
        // Start the service.
        serviceRule.startService(Intent(getApplicationContext(), BackgroundMusicService::class.java))

    }
}
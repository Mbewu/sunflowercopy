package com.example.sunflower_copy

//import android.R

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.sunflower_copy.databinding.ActivityMainBinding
import com.example.sunflower_copy.util.BackgroundMusicService
import com.example.sunflower_copy.util.setupUI
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    //private lateinit var mapFragment: MapFragment

    private var bound: Boolean = false;
    private var backgroundMusicService: BackgroundMusicService? = null

    var connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {

            Timber.i("service connected")
            val localBinder =
                service as BackgroundMusicService.LocalBinder
            backgroundMusicService = localBinder.getService()
            bound = true
        }
        override fun onServiceDisconnected(name: ComponentName) {
            Timber.i("service disconnected")
            bound = false
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get fragments instance for saving data
        if (savedInstanceState != null) {
            //Restore the fragment's instance
            //mapFragment = supportFragmentManager.getFragment(savedInstanceState, "MapFragment")
            //mapFragment = supportFragmentManager.getFragment(savedInstanceState,"MapFragment") as MapFragment
        }

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )

        setSupportActionBar(binding.toolbar)

        drawerLayout = binding.drawerLayout

        val navController = this.findNavController(R.id.nav_host_fragment)

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        NavigationUI.setupWithNavController(binding.navView, navController)

        // hide the keyboard in the toolbar mainly the issue
        setupUI(drawerLayout, this)


        // Bind to BackgroundMusicService and start
        startBackgroundMusicService()

    }

    private fun startBackgroundMusicService() {
        Timber.i("before starting background music service")
        val intent = Intent(this, BackgroundMusicService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Timber.i("after starting background music service")
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //Save the fragment's instance
        //supportFragmentManager.putFragment(outState, "MapFragment", mapFragment)
    }


    override fun onStart() {
        Timber.i("start activity")
        super.onStart()

        backgroundMusicService?.resumeMusic()
    }


    override fun onStop() {
        Timber.i("stop activity")
        backgroundMusicService?.pauseMusic()
        super.onStop()
    }


    override fun onDestroy() {
        Timber.i("destroy activity")
        super.onDestroy()
        unbindService(connection)
        bound = false
    }
}


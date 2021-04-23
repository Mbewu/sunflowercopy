package com.example.sunflower_copy.title

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.*
import com.example.sunflower_copy.R
import com.example.sunflower_copy.databinding.FragmentSettingsBinding
import com.example.sunflower_copy.databinding.FragmentTitleBinding
import com.example.sunflower_copy.databinding.FragmentViewPagerBinding
import com.example.sunflower_copy.util.BackgroundMusicService
import timber.log.Timber

class SettingsFragmentContainer : Fragment() {


    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        binding = FragmentSettingsBinding.inflate(inflater)

        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.pref_content, SettingsFragment())
            .commit()

        //return view
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

//        binding.toolbar
//            .setupWithNavController(navController, appBarConfiguration)

        binding.toolbar
            .setupWithNavController(navController, appBarConfiguration)

        (activity as AppCompatActivity).supportActionBar?.hide()
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_circle);

    }
}

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val loginViewModel by activityViewModels<LoginViewModel>() {
        LoginViewModelFactory(requireActivity().application)
    }

    private var bound: Boolean = false;
    private lateinit var backgroundMusicService: BackgroundMusicService

    private var sharedPreferencesFile: String = ""
        //

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

        Timber.i("in oncreate")
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        Timber.i("creating preferences")

        //sharedPreferencesFile = getString(R.string.preference_file_key_base).plus("_default")
        preferenceManager.sharedPreferencesName = sharedPreferencesFile

        Timber.i("using $sharedPreferencesFile")

        setPreferencesFromResource(R.xml.settings, rootKey)
        Timber.i("hi?")

        // why bother with this?
        //setInitialPreferences()
    }

    // this is a seemingly useful and often problematic function
    private fun setInitialPreferences() {


        // show the current value in the settings screen
        for(preference in preferenceScreen)
            pickPreferenceObject(preference);

    }

    private fun pickPreferenceObject(p: Preference) {
        if (p is PreferenceCategory) {
            val cat: PreferenceCategory = p;
            for (i in 0 until cat.preferenceCount) {
                pickPreferenceObject(cat.getPreference(i));
            }
        } else {
            initValues(p);
        }
    }

//<SwitchPreferenceCompat
//ListPreference
//SeekBarPreference
    private fun initValues(p: Preference) {

        // set the values for listpreferences, e.g. backgroundmusic song
        if (p is ListPreference) {
            Timber.i("setting list pref")
            val listPref: ListPreference = p
            val value = preferenceManager.sharedPreferences.getString(listPref.key, "");
            listPref.value = value
        }

        // set the values for switchpreferences, e.g. backgroundmusic on/off
        if (p is SwitchPreferenceCompat) {
            Timber.i("setting switch pref")
            val switchPref: SwitchPreferenceCompat = p
            val value = preferenceManager.sharedPreferences.getBoolean(switchPref.key, false);
            switchPref.isChecked = value
        }


        // set the values for listpreferences, e.g. backgroundmusic song
        if (p is SeekBarPreference) {
            Timber.i("setting seek pref")
            val seekPref: SeekBarPreference = p
            val value = preferenceManager.sharedPreferences.getInt(seekPref.key, 25);
            seekPref.value = value
        }



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // hmm, seems to be kicking off everytime we enter, which is okay, but weird
            loginViewModel.sharedPreferenceFile.observe(
                viewLifecycleOwner,
                Observer { sharedPreferenceFile ->
                    Timber.i("changing preferences to use file $sharedPreferenceFile")
                    this.sharedPreferencesFile = sharedPreferenceFile
                    //preferenceManager.sharedPreferencesName = this.sharedPreferencesFile
                    preferenceManager.sharedPreferencesName = sharedPreferencesFile
                    //setInitialPreferences()
                })
        } catch (e: IllegalStateException) {
            Log.e("poop", "failed", e)
        }

        // bind the backgroundMusicService
        Intent(requireActivity(), BackgroundMusicService::class.java).also { intent ->
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        Timber.i("hello, in shared preference listener")
        Timber.i("preferenceManager.sharedPreferencesName = ${preferenceManager.sharedPreferencesName}")
        when(key) {
            (getString(R.string.background_music_on_off_key)) -> {
                val backgroundMusicOnOff =
                    sharedPreferences.getBoolean(
                        key,
                        getString(R.string.background_music_on_off_default).toBoolean()
                    )
                if (backgroundMusicOnOff) {
                    backgroundMusicService.startMusic()
                } else {
                    backgroundMusicService.pauseMusic()
                }
            }

            (getString(R.string.background_music_volume_key)) -> {
                val backgroundMusicVolume =
                    sharedPreferences.getInt(
                        key,
                        getString(R.string.background_music_volume_default).toInt()
                    )
                backgroundMusicService.changeVolume(backgroundMusicVolume)
            }


            (getString(R.string.background_music_song_key)) -> {
                val backgroundMusicSong =
                    sharedPreferences.getString(
                        key,
                        getString(R.string.background_music_song_default)
                    )
                backgroundMusicSong?.let { backgroundMusicService.changeMusic(it) }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // unbind service on destroy
        requireActivity().unbindService(connection)
        bound = false
    }
}
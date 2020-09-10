package com.example.sunflower_copy.title

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.example.sunflower_copy.R
import com.example.sunflower_copy.SharedViewModel
import com.example.sunflower_copy.databinding.FragmentTitleBinding
import com.example.sunflower_copy.util.BackgroundMusicService
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 *
 */
class TitleFragment : Fragment() {

    // login stuff
    companion object {
        const val TAG = "MainFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    //private val loginViewModel by viewModels<LoginViewModel>()
//    private val loginViewModel: LoginViewModel by activityViewModels() {
//        val application = requireActivity().application
//        LoginViewModelFactory(application)
//    }

    private val loginViewModel: LoginViewModel by activityViewModels() {
        LoginViewModelFactory(requireActivity().application)
    }
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var binding: FragmentTitleBinding

    lateinit var sunflowerImage: ImageView

    private var sharedPreferenceFile: String? = null

    private var bound: Boolean = false;
    private lateinit var backgroundMusicService: BackgroundMusicService

    var connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {

            Timber.i("service connected")
            val localBinder =
                service as BackgroundMusicService.LocalBinder
            backgroundMusicService = localBinder.getService()
            bound = true
            // on connection we start the music
            setBackgroundMusic()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Timber.i("service disconnected")
            bound = false
        }

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentTitleBinding>(inflater,
            R.layout.fragment_title,container,false)

        Timber.i("what are we doing here?")
        binding.buttonStart.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_titleFragment_to_viewPagerFragment)
        }
        binding.buttonAuth.text = getString(R.string.login)

        // set sunflower animation immediately
        sunflowerImage = binding.titleImage
        animateSunflower()

        binding.buttonStopMusic.setOnClickListener {
//            val playerIntent = Intent(requireActivity(), BackgroundMusicService::class.java)
//            requireActivity().stopService(playerIntent)

            backgroundMusicService.pauseMusic()
        }

        binding.buttonStartMusic.setOnClickListener {
//            val playerIntent = Intent(requireActivity(), BackgroundMusicService::class.java)
//            requireActivity().startService(playerIntent)

            backgroundMusicService.resumeMusic()
        }

//        val playerIntent = Intent(requireActivity(), BackgroundMusicService::class.java)
//        requireActivity().startService(playerIntent)

        Intent(requireActivity(), BackgroundMusicService::class.java).also { intent ->
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        //setHasOptionsMenu(true)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.i("what are we doing here?")
        observeAuthenticationState()

        Timber.i("what are we doing here?6")

        binding.buttonAuth.setOnClickListener { launchSignInFlow() }

    }


    // when the program comes back here after firebase it comes here
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in.
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                // Sign in failed. If response is null, the user canceled the
                // sign-in flow using the back button. Otherwise, check
                // the error code and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    /**
     * Observes the authentication state and changes the UI accordingly.
     * If there is a logged in user: (1) show a logout button and (2) display their name.
     * If there is no logged in user: show a login button
     */
    private fun observeAuthenticationState() {
        loginViewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->

            Timber.i("what are we doing here?")
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    binding.buttonAuth.text = getString(R.string.logout)
                    binding.buttonAuth.setOnClickListener {
                        // TODO implement logging out user in next step
                        AuthUI.getInstance().signOut(requireContext())
                    }



                }
                else -> {
                    // TODO 3. Lastly, if there is no logged-in user...
                    binding.buttonAuth.text = getString(R.string.login)
                    binding.buttonAuth.setOnClickListener { launchSignInFlow() }

                    // change the shared preferences file
                }
            }


            Timber.i("what are we doing here?4")
        })

        loginViewModel.sharedPreferenceFile.observe(viewLifecycleOwner, Observer { sharedPreferenceFile ->

            this.sharedPreferenceFile = sharedPreferenceFile
            Timber.i("what are we doing here?5")
            if(bound)
                setBackgroundMusic()

        })

        Timber.i("what are we doing here?5")
    }

    // when the login changes, set the background music
    private fun setBackgroundMusic() {

        Timber.i("setting background music based on shared preferences")
        Timber.i("sharedPreferenceFile = ".plus(sharedPreferenceFile))

        // change the shared preferences file
        // start the music based on the users preferences
        //val sharedPreferenceFile = loginViewModel.sharedPreferenceFile.value
        val sharedPreferences = requireActivity().getSharedPreferences(
            sharedPreferenceFile,Context.MODE_PRIVATE)

        val backgroundMusicOnOffDefault =
            getString(R.string.background_music_on_off_default).toBoolean()
        val backgroundMusicOnOff =
            sharedPreferences.getBoolean(getString(R.string.background_music_on_off_key),
                backgroundMusicOnOffDefault)

        Timber.i("backgroundMusicOnOff = ".plus(backgroundMusicOnOff))
        Timber.i("backgroundMusicOnOffDefault = ".plus(backgroundMusicOnOffDefault))
        val backgroundMusicVolumeDefault =
            getString(R.string.background_music_volume_default).toInt()
        val backgroundMusicVolume =
            sharedPreferences.getInt(getString(R.string.background_music_volume_key),
                backgroundMusicVolumeDefault)

        Timber.i("backgroundMusicVolume = ".plus(backgroundMusicVolume))
        Timber.i("backgroundMusicVolumeDefault = ".plus(backgroundMusicVolumeDefault))
        val backgroundMusicSongDefault =
            getString(R.string.background_music_song_default)
        val backgroundMusicSong =
            sharedPreferences.getString(getString(R.string.background_music_song_key),
                backgroundMusicSongDefault)

        Timber.i("backgroundMusicSong = ".plus(backgroundMusicSong))
        Timber.i("backgroundMusicSongDefault = ".plus(backgroundMusicSongDefault))

        if(backgroundMusicOnOff) {
            Timber.i("changing")

            backgroundMusicService.changeMusic(backgroundMusicSong!!)
            backgroundMusicService.changeVolume(backgroundMusicVolume)
            backgroundMusicService.resumeMusic()
        } else {
            backgroundMusicService.pauseMusic()
        }
    }

//    override fun onStart() {
//        super.onStart()
//        // Bind to BackgroundMusic
//        Intent(requireActivity(), BackgroundMusicService::class.java).also { intent ->
//            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
//        }
//    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account.
        // If users choose to register with their email,
        // they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()

            // This is where you can provide more ways for users to register and
            // sign in.
        )

        // Create and launch the sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_REQUEST_CODE. (replaced with SIGN_IN_RESULT_CODE)
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }

    private fun animateSunflower() {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1.8f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1.8f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            sunflowerImage, scaleX, scaleY)
        animator.duration = 1000
        //animator.repeatCount = 1
        //animator.repeatMode = ObjectAnimator.REVERSE
        //animator.disableViewDuringAnimation(scaleButton)
        animator.start()

    }

    //
//    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater?.inflate(R.menu.options_menu,menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        return NavigationUI.onNavDestinationSelected(item!!,view!!.findNavController())
//                ||super.onOptionsItemSelected(item)
//    }
    override fun onDestroy() {
        super.onDestroy()

        // unbind service on destroy
        requireActivity().unbindService(connection)
        bound = false
    }
}

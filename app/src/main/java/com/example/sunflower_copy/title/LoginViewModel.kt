package com.example.sunflower_copy.title

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.sunflower_copy.R
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber
import kotlin.random.Random

class LoginViewModel(app: Application) : ViewModel() {

    val authenticationState = FirebaseUserLiveData().map { user ->
        Timber.i("what are we doing here?1")
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }

    }

    // get the shared preferences based on the authentication state
    val sharedPreferenceFile = Transformations.map(authenticationState) { authState ->
        Timber.i("what are we doing here?2")
        if(authState == AuthenticationState.AUTHENTICATED) {
            app.getString(R.string.preference_file_key_base)
                .plus("_").plus(FirebaseAuth.getInstance().currentUser?.displayName)
        }
        else {
            app.getString(R.string.preference_file_key_base)
                .plus("_default")
        }
    }


    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

}

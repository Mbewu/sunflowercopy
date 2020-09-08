package com.example.sunflower_copy.util

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.fragment.app.activityViewModels
import com.example.sunflower_copy.title.LoginViewModel
import com.example.sunflower_copy.title.LoginViewModelFactory


/**
 * Created by vikas on 12/14/15.
 */
// in the original, everytime you call this class you need to know the login
// i want it so that if you don't say, then it will use whatever is there
class Pref private constructor(pContext: Context, val userId: String = "current_user") {
    private val mPreferences: SharedPreferences

    companion object {
        private const val PREF_NAME = "AppName_%s"
        private var sInstance: Pref? = null
        fun getPreferences(context: Context, userId: String): Pref? {
            if (null != sInstance || !TextUtils.equals(sInstance!!.userId, userId)) {
                sInstance = Pref(context, userId)
            }
            return sInstance
        } //write methods for updating in preference files
    }

    init {
        mPreferences = pContext.getSharedPreferences(
            String.format(
                PREF_NAME,
                userId
            ), Context.MODE_PRIVATE
        )
    }
}

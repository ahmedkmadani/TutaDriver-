package com.tutaap.tutadriver

import android.content.Context
import android.content.Intent

class SharedPrefManager private constructor(context: Context) {


    val isLoggedIn: Boolean
        get() {
            val sharedPreferences = ctx?.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            return sharedPreferences?.getString(KEY_TOKEN, null) != null
        }


    val user: User
        get() {
            val sharedPreferences = ctx?.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            return User(
                sharedPreferences!!.getString(KEY_ID, null),
                sharedPreferences!!.getString(KEY_FirstNAME, null),
                sharedPreferences!!.getString(KEY_LASTNAME, null),
                sharedPreferences!!.getString(KEY_EMAIL, null),
                sharedPreferences!!.getString(KEY_DELETEAT, null),
                sharedPreferences!!.getString(KEY_CREATEDAT, null),
                sharedPreferences!!.getString(KEY_UPDATEDAT, null),
                sharedPreferences!!.getString(KEY_VERIFIEDAT, null),
                sharedPreferences!!.getInt(KEY_IS_DRIVER, -1),
                sharedPreferences!!.getString(KEY_TOKEN, null)

            )
        }

    init {
        ctx = context
    }



    fun userLogin(user: User) {
        val sharedPreferences = ctx?.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString(KEY_TOKEN, user.token)
        editor?.putString(KEY_ID, user.Id)
        editor?.putString(KEY_FirstNAME, user.FirstName)
        editor?.putString(KEY_LASTNAME, user.LastName)
        editor?.putInt(KEY_IS_DRIVER, user.is_driver)
        editor?.putString(KEY_EMAIL, user.Email)
        editor?.putString(KEY_DELETEAT, user.DeletedAt)
        editor?.putString(KEY_CREATEDAT, user.CreatedAt)
        editor?.putString(KEY_UPDATEDAT, user.UpdatedAt)
        editor?.putString(KEY_VERIFIEDAT, user.EmailVerifiedAt)

        editor?.apply()
    }



    fun logout() {
        val sharedPreferences = ctx?.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.clear()
        editor?.apply()
        ctx?.startActivity(Intent(ctx, SignupActivity::class.java))
    }

    companion object {

        private val SHARED_PREF_NAME = "Tuta"
        private val KEY_TOKEN = "keytoken"
        private val KEY_FirstNAME = "keyusername"
        private val KEY_LASTNAME = "keyfirstname"
        private val KEY_EMAIL = "keyemail"
        private val KEY_ID = "keyid"
        private val KEY_DELETEAT = "keydelete"
        private val KEY_CREATEDAT = "keycreater"
        private val KEY_UPDATEDAT = "keyupdate"
        private val KEY_IS_DRIVER = "keyisdriver"
        private val KEY_VERIFIEDAT = "keyverfied"


        private var mInstance: SharedPrefManager? = null
        private var ctx: Context? = null
        @Synchronized
        fun getInstance(context: Context): SharedPrefManager {
            if (mInstance == null) {
                mInstance = SharedPrefManager(context)
            }
            return mInstance as SharedPrefManager
        }
    }
}


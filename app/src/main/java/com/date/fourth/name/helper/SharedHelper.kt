package com.example.cacso.helper

import android.content.Context
import android.content.SharedPreferences
import com.date.fourth.name.Constants

class SharedHelper {


    companion object {
        val LAST_LINK = "last_link"
        var sharedPreferences: SharedPreferences? = null

        private fun getSharedPref(context: Context): SharedPreferences? {
            if (sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("app_data", Context.MODE_PRIVATE)
            }
            return sharedPreferences
        }

        private fun getSharedPrefEditor(context: Context): SharedPreferences.Editor? {
            return getSharedPref(context)?.edit()
        }

        public fun saveString(context: Context, key: String, value: String) {
            getSharedPrefEditor(context)?.putString(key, value)?.apply()
        }


        public fun getString(context: Context, key: String): String? {
            return getSharedPref(context)?.getString(key, null)
        }

        public fun getBoolean(context: Context, key: String): Boolean? {
            return getSharedPref(context)?.getBoolean(key, false)
        }

    }
}
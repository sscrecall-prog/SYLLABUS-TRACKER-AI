package com.example.util

import android.content.Context
import android.content.SharedPreferences

object OnboardingPreferences {
    private const val PREFS_NAME = "syllabus_tracker_onboarding_prefs"
    private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isOnboardingCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }
}

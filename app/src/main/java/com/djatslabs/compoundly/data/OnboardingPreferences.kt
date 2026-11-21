package com.djatslabs.compoundly.data

import android.content.Context

interface OnboardingPreferences {
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean = true)
}

class SharedPreferencesOnboardingPreferences(
    context: Context
) : OnboardingPreferences {

    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    private companion object {
        const val PREFS_NAME = "compoundly_prefs"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}

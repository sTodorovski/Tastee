package com.example.tastee.config

import android.content.Context
import com.example.tastee.R

object StripeConfig {
    fun getPublishableKey(context: Context): String {
        return context.getString(R.string.stripe_publishable_key)
    }
}
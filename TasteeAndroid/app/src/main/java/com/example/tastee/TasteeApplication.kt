package com.example.tastee

import android.app.Application
import android.os.Build
import com.stripe.android.PaymentConfiguration
import com.example.tastee.config.StripeConfig
import com.example.tastee.utils.DeliveryNotificationHelper

class TasteeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DeliveryNotificationHelper.createNotificationChannel(this)
        }

        PaymentConfiguration.init(
            applicationContext,
            StripeConfig.getPublishableKey(applicationContext)
        )
    }
}
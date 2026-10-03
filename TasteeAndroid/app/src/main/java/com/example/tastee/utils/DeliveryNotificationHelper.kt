package com.example.tastee.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.tastee.ActiveDeliveryActivity
import com.example.tastee.R

object DeliveryNotificationHelper {
    private const val CHANNEL_ID = "delivery_channel"
    private const val NOTIFICATION_ID = 1001

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Deliveries",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for active deliveries"
            enableVibration(true)
            enableLights(true)
            lightColor = Color.BLUE
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDeliveryNotification(
        context: Context,
        orderId: Long,
        restaurantName: String,
        deliveryAddress: String
    ) {
        val intent = Intent(context, ActiveDeliveryActivity::class.java).apply {
            putExtra("ORDER_ID", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_delivery)
            .setContentTitle("Delivery in progress")
            .setContentText("Order #$orderId · $restaurantName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Order #$orderId\n$restaurantName\nDeliver to: $deliveryAddress")
            )
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancelDeliveryNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
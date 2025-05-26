package com.loopcutmini.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationChannelManager(private val context: Context) {
    companion object {
        const val CHANNEL_ID_MONITORING = "loopcut_monitoring"
        const val CHANNEL_ID_ACTION = "loopcut_action"
        const val CHANNEL_ID_BATTERY = "loopcut_battery"
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // モニタリングチャンネル
            val monitoringChannel = NotificationChannel(
                CHANNEL_ID_MONITORING,
                "反芻思考監視",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "反芻思考の監視状態を通知"
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }

            // アクションチャンネル
            val actionChannel = NotificationChannel(
                CHANNEL_ID_ACTION,
                "中断アクション",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "中断アクションの通知"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            // バッテリー最適化チャンネル
            val batteryChannel = NotificationChannel(
                CHANNEL_ID_BATTERY,
                "バッテリー最適化",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "バッテリー最適化の通知"
                enableLights(true)
                enableVibration(false)
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(
                listOf(monitoringChannel, actionChannel, batteryChannel)
            )
        }
    }

    fun createMonitoringNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID_MONITORING)
            .setContentTitle("LoopCut Mini")
            .setContentText("反芻思考を監視中")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    fun createActionNotification(title: String, message: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID_ACTION)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    fun createBatteryNotification(message: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID_BATTERY)
            .setContentTitle("バッテリー最適化")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }
}

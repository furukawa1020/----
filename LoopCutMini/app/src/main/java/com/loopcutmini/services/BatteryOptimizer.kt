package com.loopcutmini.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

class BatteryOptimizer(private val context: Context) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val wakeLock: PowerManager.WakeLock
    private val CHECK_INTERVAL = 3000L // 3秒間隔
    private val LOW_BATTERY_THRESHOLD = 15 // バッテリー残量の閾値（%）

    init {
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "LoopCutMini:BatteryOptimizer"
        )
    }

    fun startOptimization() {
        // バッテリー状態の監視を開始
        startBatteryMonitoring()
        
        // バッテリー最適化の設定
        setupBatteryOptimization()
    }

    private fun startBatteryMonitoring() {
        val intent = Intent(context, BatteryMonitorReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            CHECK_INTERVAL,
            pendingIntent
        )
    }

    private fun setupBatteryOptimization() {
        // バッテリー最適化の設定
        val batteryOptimizationIntent = Intent(
            android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        ).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }

        // バッテリー最適化の状態を確認
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            // バッテリー最適化を無効にするためのIntentを生成
            // アプリ起動時にユーザーに通知を表示
            val notification = NotificationCompat.Builder(context, "battery_channel")
                .setContentTitle("バッテリー最適化の設定")
                .setContentText("アプリケーションの正常な動作のため、バッテリー最適化を無効にしてください")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.notify(1, notification)
        }
    }

    fun acquireWakeLock() {
        if (!wakeLock.isHeld) {
            wakeLock.acquire(CHECK_INTERVAL)
        }
    }

    fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    fun isLowBattery(): Boolean {
        val batteryInfo = powerManager.batteryInfo
        return batteryInfo?.level ?: 100 < LOW_BATTERY_THRESHOLD
    }

    companion object {
        fun getBatteryLevel(context: Context): Int {
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            return batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        }
    }
}

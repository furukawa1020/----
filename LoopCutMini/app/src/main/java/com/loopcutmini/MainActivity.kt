package com.loopcutmini

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.loopcutmini.BATTERY_LEVEL_CHANGED
import com.loopcutmini.services.*
import com.loopcutmini.services.NotificationChannelManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var batteryOptimizer: BatteryOptimizer
    private lateinit var notificationManager: NotificationChannelManager
    private var isMonitoring = false
    private val batteryStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BATTERY_LEVEL_CHANGED) {
                val level = intent.getIntExtra("battery_level", 0)
                updateBatteryStatus(level)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // サービスの初期化
        batteryOptimizer = BatteryOptimizer(this)
        notificationManager = NotificationChannelManager(this)
        
        // 通知チャンネルの作成
        notificationManager.createNotificationChannels()
        
        // UIの初期化
        initializeUI()
        
        // サービスの開始
        startMonitoringService()
        
        // バッテリーレシーバーの登録
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(batteryStatusReceiver, IntentFilter(BATTERY_LEVEL_CHANGED))
    }

    override fun onDestroy() {
        // バッテリーレシーバーの解除
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(batteryStatusReceiver)
        
        stopMonitoringService()
        super.onDestroy()
    }
    
    // ... 既存のコード ...

package com.loopcutmini.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.loopcutmini.BATTERY_LEVEL_CHANGED

class BatteryMonitorReceiver : BroadcastReceiver() {
    private val TAG = "BatteryMonitorReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = (level / scale.toFloat()) * 100
                
                // バッテリー状態の変更をメインアクティビティに通知
                val broadcastIntent = Intent(BATTERY_LEVEL_CHANGED)
                broadcastIntent.putExtra("battery_level", batteryPct.toInt())
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
                
                Log.d(TAG, "Battery Level: $batteryPct%")
                
                // バッテリーが低い場合の処理
                if (batteryPct < 15) {
                    // 音声認識の頻度を下げる
                    // バッテリー使用量を最適化
                }
            }
        }
    }
}

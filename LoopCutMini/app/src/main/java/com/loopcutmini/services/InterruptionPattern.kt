package com.loopcutmini.services

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

class InterruptionPattern(private val context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .build()
    
    private val vibrator by lazy {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    }
    
    private val interruptionSounds = mutableMapOf<String, Int>()
    private val soundResources = mapOf(
        "click" to R.raw.click_sound,
        "chime" to R.raw.chime_sound,
        "whistle" to R.raw.whistle_sound
    )
    
    init {
        // サウンドのロード
        soundResources.forEach { (key, resourceId) ->
            interruptionSounds[key] = soundPool.load(context, resourceId, 1)
        }
    }

    // パターンショック
    fun triggerPatternShock() {
        // システムのデフォルト音を再生
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringtone?.play()
    }

    // ハプティックブレス
    fun triggerHapticBreath() {
        val pattern = longArrayOf(
            0,  // Start immediately
            100, // Vibrate for 100ms
            1000, // Wait 1000ms
            100, // Vibrate for 100ms
            1000, // Wait 1000ms
            100, // Vibrate for 100ms
            1000, // Wait 1000ms
            100, // Vibrate for 100ms
            1000, // Wait 1000ms
            100  // Vibrate for 100ms
        )
        
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    // マイクロアクション
    fun triggerMicroAction() {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        val actions = listOf(
            "肩を5回回す",
            "目を閉じて外音を3つ探す",
            "両手を握りしめる",
            "深呼吸を3回する",
            "椅子から立ち上がる"
        )

        // ランダムにアクションを選択
        val action = actions.random()
        
        val notification = NotificationCompat.Builder(context, "action_channel")
            .setContentTitle("中断タスク")
            .setContentText(action)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }

    fun releaseResources() {
        soundPool.release()
    }
}

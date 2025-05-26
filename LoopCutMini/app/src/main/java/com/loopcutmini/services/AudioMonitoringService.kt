package com.loopcutmini.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.*
import androidx.core.app.NotificationCompat
import com.loopcutmini.R
import org.apache.commons.math3.stat.Frequency
import kotlin.math.roundToInt

class AudioMonitoringService : Service() {
    private lateinit var voiceEngine: VoiceRecognitionEngine
    private lateinit var batteryOptimizer: BatteryOptimizer
    private lateinit var patternDetector: NegativePatternDetector
    private lateinit var interruptionPattern: InterruptionPattern
    private var isRunning = false
    private val frequencyCounter = Frequency()
    private val handler = Handler(Looper.getMainLooper())
    private val NORMAL_INTERVAL = 3000L // 正常時の監視間隔 (3秒)
    private val LOW_BATTERY_INTERVAL = 6000L // 低バッテリー時の監視間隔 (6秒)
    private var currentInterval = NORMAL_INTERVAL

    override fun onCreate() {
        super.onCreate()
        setupForegroundService()
        voiceEngine = VoiceRecognitionEngine(this)
        batteryOptimizer = BatteryOptimizer(this)
        patternDetector = NegativePatternDetector()
        interruptionPattern = InterruptionPattern(this)
        batteryOptimizer.startOptimization()
    }

    private fun setupForegroundService() {
        val notification = NotificationCompat.Builder(this, "loopcut_channel")
            .setContentTitle("LoopCut Mini")
            .setContentText("反芻思考を監視中")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        val recordingThread = Thread {
            voiceEngine.startRecording()
            var lastDetectionTime = System.currentTimeMillis()

            while (isRunning) {
                try {
                    // バッテリー状態に応じて監視間隔を調整
                    if (batteryOptimizer.isLowBattery()) {
                        currentInterval = LOW_BATTERY_INTERVAL
                    } else {
                        currentInterval = NORMAL_INTERVAL
                    }

                    val transcription = voiceEngine.processAudio()
                    transcription?.let { text ->
                        val isNegative = patternDetector.detectNegativePattern(text)

                        if (isNegative) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastDetectionTime < 90000) {
                                triggerInterruption()
                                lastDetectionTime = currentTime
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Thread.sleep(currentInterval)
            }
            voiceEngine.stopRecording()
        }
        recordingThread.start()
    }

    private fun triggerInterruption() {
        // 1. Pattern-Shock (2kHz click)
        interruptionPattern.triggerPatternShock()
        
        // 2. Haptic-Breath (0.1Hz pulse vibration)
        interruptionPattern.triggerHapticBreath()
        
        // 3. Micro-Action notification
        interruptionPattern.triggerMicroAction()
    }

    override fun onDestroy() {
        isRunning = false
        voiceEngine.release()
        batteryOptimizer.releaseWakeLock()
        patternDetector.resetDetection()
        interruptionPattern.releaseResources()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

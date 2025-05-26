package com.loopcutmini.services

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import kotlin.math.*

class AudioProcessor {
    companion object {
        private const val SAMPLE_RATE = 16000
        private const val FRAME_SIZE = 1024
        private const val HOP_SIZE = 256
        private const val MEL_BINS = 40
        private const val N_FFT = 1024
        private const val MIN_HZ = 0.0
        private const val MAX_HZ = SAMPLE_RATE / 2.0
    }

    private val fft = FastFourierTransformer(DftNormalization.STANDARD)
    private val melFilterBank = createMelFilterBank()
    private val window = createHanningWindow()
    private val audioRecord: AudioRecord
    private var isRecording = false
    private val audioBuffer = ShortArray(FRAME_SIZE)
    private val audioQueue = ArrayDeque<ShortArray>()
    private val processorThread = ProcessorThread()
    private val lock = Any()

    init {
        // オーディオレコードの初期化
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            FRAME_SIZE * 2
        )

        // メルフィルタバンクの初期化
        createMelFilterBank()
    }

    private fun createHanningWindow(): FloatArray {
        return FloatArray(FRAME_SIZE) { i ->
            0.5f * (1.0f - cos(2.0f * PI * i / (FRAME_SIZE - 1)))
        }
    }

    private fun createMelFilterBank(): Array<FloatArray> {
        val melMax = 2595 * log10(1 + MAX_HZ / 700)
        val melPoints = FloatArray(MEL_BINS + 2)
        val hzPoints = FloatArray(MEL_BINS + 2)
        
        for (i in melPoints.indices) {
            melPoints[i] = i * melMax / (MEL_BINS + 1)
            hzPoints[i] = 700 * (10.0.pow(melPoints[i] / 2595) - 1)
        }

        val filterBank = Array(MEL_BINS) { FloatArray(N_FFT / 2 + 1) }
        
        for (m in 0 until MEL_BINS) {
            val fM = hzPoints[m]
            val fM1 = hzPoints[m + 1]
            val fM2 = hzPoints[m + 2]
            
            for (k in 0 until N_FFT / 2 + 1) {
                val fK = k * SAMPLE_RATE / N_FFT
                
                if (fK >= fM && fK < fM1) {
                    filterBank[m][k] = (fK - fM) / (fM1 - fM)
                } else if (fK >= fM1 && fK < fM2) {
                    filterBank[m][k] = (fM2 - fK) / (fM2 - fM1)
                } else {
                    filterBank[m][k] = 0.0f
                }
            }
        }
        
        return filterBank
    }

    fun startProcessing() {
        synchronized(lock) {
            if (isRecording) return
            isRecording = true
            audioRecord.startRecording()
            processorThread.start()
        }
    }

    fun stopProcessing() {
        synchronized(lock) {
            if (!isRecording) return
            isRecording = false
            audioRecord.stop()
            processorThread.interrupt()
        }
    }

    private inner class ProcessorThread : Thread() {
        override fun run() {
            while (isRecording) {
                try {
                    val bytesRead = audioRecord.read(audioBuffer, 0, FRAME_SIZE)
                    if (bytesRead > 0) {
                        synchronized(lock) {
                            audioQueue.add(audioBuffer.copyOf())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

            // FFT実行
            val fftResult = performFFT(windowed)

            // メルスペクトログラム計算
            val melSpectrum = FloatArray(MEL_FILTERS)
            for (i in melFilters.indices) {
                var sum = 0.0f
                for (j in fftResult.indices) {
                    sum += fftResult[j] * melFilters[i][j]
                }
                melSpectrum[i] = log(max(sum, 1e-10f))
            }

            // 特徴量を追加
            features.addAll(melSpectrum.asList())
        }

        return features.toFloatArray()
    }

    private fun performFFT(data: FloatArray): FloatArray {
        // ここに実際のFFT実装を追加
        // 現在はダミーの実装
        return data
    }

    private fun hzToMel(hz: Double): Double {
        return 2595.0 * log10(1 + hz / 700.0)
    }

    private fun melToHz(mel: Double): Double {
        return 700.0 * (exp(mel / 2595.0) - 1)
    }
}

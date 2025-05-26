package com.loopcutmini.services

import android.content.Context
import android.util.Log
import java.io.File

class ModelManager(private val context: Context) {
    companion object {
        private const val MODEL_FILE_NAME = "whisper-tiny.tflite"
        private const val TAG = "ModelManager"
    }

    fun getModelPath(): String {
        return context.filesDir.absolutePath + File.separator + MODEL_FILE_NAME
    }

    fun isModelAvailable(): Boolean {
        val file = File(getModelPath())
        return file.exists() && file.length() > 0
    }

    fun downloadModelIfNeeded(): Boolean {
        if (isModelAvailable()) {
            Log.i(TAG, "Model already exists")
            return true
        }

        Log.i(TAG, "Downloading model...")
        ModelDownloader(context).downloadModel()
        return isModelAvailable()
    }

    fun deleteModel() {
        val file = File(getModelPath())
        if (file.exists()) {
            file.delete()
            Log.i(TAG, "Model deleted")
        }
    }
}

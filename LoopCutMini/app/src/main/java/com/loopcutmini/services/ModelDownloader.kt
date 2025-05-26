package com.loopcutmini.services

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloader(private val context: Context) {
    companion object {
        private const val MODEL_URL = "https://github.com/openai/whisper/releases/download/v20230927/whisper-tiny.tflite"
        private const val MODEL_FILE_NAME = "whisper-tiny.tflite"
        private const val TAG = "ModelDownloader"
    }

    fun downloadModel() {
        DownloadTask().execute()
    }

    private inner class DownloadTask : AsyncTask<Void, Int, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            try {
                val url = URL(MODEL_URL)
                val connection = url.openConnection() as HttpURLConnection
                
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(getModelFilePath())
                
                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead = 0
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    publishProgress((totalBytesRead * 100 / connection.contentLength).toInt())
                }
                
                inputStream.close()
                outputStream.close()
                
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading model", e)
                return false
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            // ダウンロードの進行状況をUIに反映
        }

        override fun onPostExecute(result: Boolean?) {
            if (result == true) {
                Log.i(TAG, "Model downloaded successfully")
            } else {
                Log.e(TAG, "Failed to download model")
            }
        }
    }

    private fun getModelFilePath(): String {
        return context.filesDir.absolutePath + File.separator + MODEL_FILE_NAME
    }
}

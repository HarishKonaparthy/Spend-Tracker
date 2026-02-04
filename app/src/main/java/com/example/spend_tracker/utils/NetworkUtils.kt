package com.example.spend_tracker.network

import kotlinx.coroutines.*
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {

    interface DataFetchCallback {
        fun onDataFetched(data: JSONArray)
        fun onError(error: String)
    }

    fun fetchDataFromUrl(urlString: String, callback: DataFetchCallback) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val response = conn.inputStream.bufferedReader().readText()

                conn.disconnect()

                val jsonArray = JSONArray(response)

                withContext(Dispatchers.Main) {
                    callback.onDataFetched(jsonArray)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError("Error: ${e.message}")
                }
            }
        }
    }
}
package com.example.myapplication

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.GzipSource
import okio.buffer
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class RouteInfo(val routeDepDesInfo: String, val arrivalTimeInfoDirection0: String, val arrivalTimeInfoDirection1: String)

object Route_arrivetime {
    suspend fun main(subRouteName: String): RouteInfo {
        val tokenUrl = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
        val clientId = "s11026310-7c639d60-e149-4847" // clientId
        val clientSecret = "a1e0f98b-ff0c-44bb-80b7-cb9c6ebad7e6" // clientSecret

        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val tokenInfo = withContext(Dispatchers.IO) { getAccessToken(tokenUrl, clientId, clientSecret) }
        val tokenElem: JsonNode = objectMapper.readTree(tokenInfo)
        val accessToken: String = tokenElem.get("access_token").asText()

        // Call with correct parameters
        val routeDepDesInfo = withContext(Dispatchers.IO) { getRouteDepDesInfo(accessToken,subRouteName) }
        val arrivalTimeInfoDirection0 = withContext(Dispatchers.IO) { getArrivalTimeInfo(accessToken, subRouteName, 0) }
        val arrivalTimeInfoDirection1 = withContext(Dispatchers.IO) { getArrivalTimeInfo(accessToken, subRouteName, 1) }

        return RouteInfo(routeDepDesInfo, arrivalTimeInfoDirection0, arrivalTimeInfoDirection1)
    }

    private suspend fun getArrivalTimeInfo(accessToken: String, subRouteName: String, directionFilter: Int): String {
        // Update URL to use subRouteName
        val tdxUrl = "https://tdx.transportdata.tw/api/basic/v2/Bus/EstimatedTimeOfArrival/City/Taoyuan/$subRouteName?%24format=JSON"

        return getJsonString(tdxUrl, accessToken) { jsonString ->
            val objectMapper = ObjectMapper()
            val jsonNodes = objectMapper.readTree(jsonString)

            val result = StringBuilder()

            // 取得並顯示 RouteName
            val routeNameZhTw = jsonNodes[0]["RouteName"]["Zh_tw"].asText()
            result.append("路線名稱: $routeNameZhTw\n\n")

            // 處理去程和返程
            val stopInfos = mutableListOf<Pair<Int, String>>()

            for (node in jsonNodes) {
                val dir = node["Direction"].asInt()
                if (dir != directionFilter) continue // Filter by direction
                val stopSequence = node["StopSequence"].asInt()
                val stopName = node["StopName"]["Zh_tw"].asText()
                val stopStatus = node["StopStatus"].asInt()
                val estimateTime = node["EstimateTime"]?.asInt() ?: -1 // Extract EstimateTime, -1 if not present
                val nextBusTime = node["NextBusTime"].asText(null) // Extract NextBusTime, null if not present

                val nextBusTimeFormatted = nextBusTime?.let {
                    try {
                        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                        val targetFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val date = originalFormat.parse(it)

                        // 加上8小時配合台灣時區
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"))
                        calendar.time = date
                        val adjustedDate = calendar.time

                        targetFormat.format(adjustedDate)
                    } catch (e: Exception) {
                        null
                    }
                }

                val stopDisplay = when (stopStatus) {
                    0 -> {
                        when (estimateTime) {
                            in 0..60 -> "$stopName (進站中)"
                            in 61..150 -> "$stopName (即將進站)"
                            else -> "$stopName (${estimateTime / 60} 分鐘)"
                        }
                    }
                    1 -> nextBusTimeFormatted?.let { "$stopName ($it)" } ?: "$stopName (無資料)"
                    else -> "$stopName (末班駛離)"  // else代表stopStatus的值為3
                }

                stopInfos.add(Pair(stopSequence, stopDisplay))
            }

            // 按照 StopSequence 排序並顯示
            stopInfos.sortedBy { it.first }.forEach { result.append("${it.second}\n") }

            result.toString()
        }
    }

    private suspend fun getRouteDepDesInfo(accessToken: String, subRouteName: String): String {
        val tdxUrl = "https://tdx.transportdata.tw/api/basic/v2/Bus/Route/City/Taoyuan/$subRouteName?%24format=JSON"
        return getJsonString(tdxUrl, accessToken) { jsonString ->
            val objectMapper = ObjectMapper()
            val jsonNodes = objectMapper.readTree(jsonString)
            val result = StringBuilder()
            val departureStopNameZh = jsonNodes[0]["DepartureStopNameZh"].asText()
            val destinationStopNameZh = jsonNodes[0]["DestinationStopNameZh"].asText()
            result.append("起點: $departureStopNameZh\n")
            result.append("終點: $destinationStopNameZh\n")
            result.toString()
        }
    }

    @Throws(IOException::class)
    private fun getAccessToken(tokenUrl: String, clientId: String, clientSecret: String): String {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val formBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .build()

        val request = Request.Builder()
            .url(tokenUrl)
            .post(formBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            return response.body?.string() ?: throw IOException("Empty response body")
        }
    }

    @Throws(IOException::class)
    private fun getJsonString(url: String, accessToken: String, processJson: (String) -> String): String {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept-Encoding", "gzip")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body ?: throw IOException("Empty response body")

            val jsonString = if ("gzip".equals(response.header("Content-Encoding"), ignoreCase = true)) {
                // Decompress gzip data
                responseBody.source().use { source ->
                    GzipSource(source).buffer().use { gzipBuffer ->
                        gzipBuffer.readUtf8()
                    }
                }
            } else {
                responseBody.string()
            }

            return processJson(jsonString)
        }
    }
}

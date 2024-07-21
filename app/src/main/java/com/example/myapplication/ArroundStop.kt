package com.example.myapplication

import android.util.Log
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.GzipSource
import okio.buffer
import java.io.IOException
import java.util.concurrent.TimeUnit

data class StopData(val stopName: String, val routeNames: List<String>)

object ArroundStop {

    suspend fun fetchStopData(latitude: Double, longitude: Double): String {
        val tokenUrl = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
        val tdxUrl = createUrlWithCoordinates(latitude, longitude)
        val clientId = "11026349-b9820ce1-cd51-4721"
        val clientSecret = "c02bf37f-9945-4fcd-bb6d-8a4a2769716c"

        val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val tokenInfo = withContext(Dispatchers.IO) { getAccessToken(tokenUrl, clientId, clientSecret) }
        val tokenElem: JsonNode = objectMapper.readTree(tokenInfo)
        val accessToken: String = tokenElem.get("access_token").asText()
        Log.d("ArroundStop", "Access Token: $accessToken") // Debug log
        val stopDataList = withContext(Dispatchers.IO) { getStopString(tdxUrl, accessToken) }
        Log.d("ArroundStop", "Stop Data List: $stopDataList") // Debug log

        return formatStopData(stopDataList)
    }

    fun createUrlWithCoordinates(latitude: Double, longitude: Double): String {
        val latRange = 0.0018
        val lonRange = 0.0019
        val minLat = latitude - latRange
        val maxLat = latitude + latRange
        val minLon = longitude - lonRange
        val maxLon = longitude + lonRange

        return "https://tdx.transportdata.tw/api/basic/v2/Bus/Station/City/Taoyuan?%24filter=StationPosition%2FPositionLat%20ge%20$minLat%20and%20StationPosition%2FPositionLat%20le%20$maxLat%20and%20StationPosition%2FPositionLon%20ge%20$minLon%20and%20%20StationPosition%2FPositionLon%20le%20$maxLon&%24format=JSON"
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
    private fun getStopString(url: String, accessToken: String): List<StopData> {
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
                responseBody.source().use { source ->
                    GzipSource(source).buffer().use { gzipBuffer ->
                        gzipBuffer.readUtf8()
                    }
                }
            } else {
                responseBody.string()
            }

            val objectMapper = ObjectMapper()
            val jsonNodes = objectMapper.readTree(jsonString)

            val stopDataMap = mutableMapOf<String, MutableSet<String>>()

            jsonNodes.forEach { stationNode ->
                val stopsNode = stationNode.get("Stops")
                if (stopsNode.isEmpty) {
                    stopDataMap["附近200公尺內無站牌!"] = mutableSetOf()
                } else {
                    stopsNode.forEach { stopNode ->
                        val stopName = stopNode.get("StopName").get("Zh_tw").asText()
                        val routeName = stopNode.get("RouteName").get("Zh_tw").asText()
                        stopDataMap.computeIfAbsent(stopName) { mutableSetOf() }.add(routeName)
                    }
                }
            }

            return stopDataMap.map { StopData(it.key, it.value.toList()) }
        }
    }

    private fun formatStopData(stopDataList: List<StopData>): String {
        return stopDataList.joinToString(separator = "") { stopData ->
            "站牌名稱: ${stopData.stopName}\n所有路線: ${stopData.routeNames.joinToString(", ")}\n\n"
        }
    }
}
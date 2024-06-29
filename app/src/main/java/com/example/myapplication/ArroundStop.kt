package com.example.myapplication

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
    suspend fun main(): String {
        val tokenUrl = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
        val tdxUrl = "https://tdx.transportdata.tw/api/basic/v2/Bus/Station/City/Taoyuan?%24filter=StationPosition%2FPositionLat%20ge%2025.0094%20and%20StationPosition%2FPositionLat%20le%2025.0184%20and%20StationPosition%2FPositionLon%20ge%20121.22313%20and%20%20StationPosition%2FPositionLon%20le%20121.23213&%24format=JSON"
        val clientId = "sherrysweet28605520-0d7e0818-4151-4795" // your clientId
        val clientSecret = "797fef62-dd98-4e6f-9af4-7e116f979896" // your clientSecret

        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val tokenInfo = withContext(Dispatchers.IO) { getAccessToken(tokenUrl, clientId, clientSecret) }
        val tokenElem: JsonNode = objectMapper.readTree(tokenInfo)
        val accessToken: String = tokenElem.get("access_token").asText()
        val stopDataList = withContext(Dispatchers.IO) { getStopString(tdxUrl, accessToken) }

        // 將 stopDataList 格式化為字符串
        return formatStopData(stopDataList)
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
                // Decompress gzip data
                responseBody.source().use { source ->
                    GzipSource(source).buffer().use { gzipBuffer ->
                        gzipBuffer.readUtf8()
                    }
                }
            } else {
                responseBody.string()
            }

            // 解析 JSON 並返回所需的數據
            val objectMapper = ObjectMapper()
            val jsonNodes = objectMapper.readTree(jsonString)

            val stopDataMap = mutableMapOf<String, MutableSet<String>>()

            jsonNodes.forEach { stationNode ->
                val stopsNode = stationNode.get("Stops")
                stopsNode.forEach { stopNode ->
                    val stopName = stopNode.get("StopName").get("Zh_tw").asText()
                    val routeName = stopNode.get("RouteName").get("Zh_tw").asText()
                    stopDataMap.computeIfAbsent(stopName) { mutableSetOf() }.add(routeName)
                }
            }

            return stopDataMap.map { StopData(it.key, it.value.toList()) }
        }
    }
    private fun formatStopData(stopDataList: List<StopData>): String {
        return stopDataList.joinToString(separator = "\n") {
            stopData ->
            "站牌名稱: ${stopData.stopName}\n所有路線: ${stopData.routeNames.joinToString(", ")}\n\n"
        }
    }
}

package com.example.myapplication

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.GzipSource
import okio.buffer

data class StopInfo(val markname: String, val formattedLongitude: String, val formattedLatitude: String)

object Stop_filter {
    suspend fun main(url: String): List<StopInfo> {
        val tokenUrl = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
        val clientId = "11026349-b9820ce1-cd51-4721" // clientId
        val clientSecret = "c02bf37f-9945-4fcd-bb6d-8a4a2769716c" // clientSecret

        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val tokenInfo = withContext(Dispatchers.IO) { getAccessToken(tokenUrl, clientId, clientSecret) }
        val tokenElem: JsonNode = objectMapper.readTree(tokenInfo)
        val accessToken: String = tokenElem.get("access_token").asText()
        val jsonString = withContext(Dispatchers.IO) { getJsonString(url, accessToken) }

        return parseJson(jsonString)
    }

    @Throws(IOException::class)
    fun getAccessToken(tokenUrl: String, clientId: String, clientSecret: String): String {
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
    fun getJsonString(url: String, accessToken: String): String {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept-Encoding", "gzip")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body ?: throw IOException("Empty response body")

            return if ("gzip".equals(response.header("Content-Encoding"), ignoreCase = true)) {

                responseBody.source().use { source ->
                    GzipSource(source).buffer().use { gzipBuffer ->
                        gzipBuffer.readUtf8()
                    }
                }
            } else {
                responseBody.string()
            }
        }
    }

    fun parseJson(jsonString: String): List<StopInfo> {
        val objectMapper = ObjectMapper()
        val rootNode = objectMapper.readTree(jsonString)
        val stopInfoList = mutableListOf<StopInfo>()

        if (rootNode.isEmpty) {
            println("200公尺內無站牌!")
            return emptyList()
        }

        for (node in rootNode) {
            val markname = node.get("Markname")?.asText() ?: ""
            val geometry = node.get("Geometry")?.asText() ?: ""
            val coordinates = geometry.removePrefix("POINT (").removeSuffix(")").split(" ")
            val longitude = coordinates.getOrNull(0)?.toDoubleOrNull() ?: continue
            val latitude = coordinates.getOrNull(1)?.toDoubleOrNull() ?: continue

            val formattedLongitude = "%.4f".format(longitude)
            val formattedLatitude = "%.4f".format(latitude)

            // 確認經緯度是否在範圍內
            val lonInRange = formattedLongitude.toDouble() in 120.9867..121.5040
            val latInRange = formattedLatitude.toDouble() in 24.6324..25.1171

            if (lonInRange && latInRange) {
                stopInfoList.add(StopInfo(markname, formattedLongitude, formattedLatitude))
            }
        }

        return stopInfoList
    }
}

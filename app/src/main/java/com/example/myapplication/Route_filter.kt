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
data class SubRouteData(val subRouteName: String, val headsign: String)
object Route_filter {
    suspend fun main(): String {
        val tokenUrl = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
        val tdxUrl = "https://tdx.transportdata.tw/api/basic/v2/Bus/Route/City/Taoyuan/15?%24format=JSON"

        val clientId = "sherrysweet28605520-0d7e0818-4151-4795" // clientId
        val clientSecret = "797fef62-dd98-4e6f-9af4-7e116f979896" // clientSecret

        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val tokenInfo = withContext(Dispatchers.IO) { getAccessToken(tokenUrl, clientId, clientSecret) }
        val tokenElem: JsonNode = objectMapper.readTree(tokenInfo)
        val accessToken: String = tokenElem.get("access_token").asText()
        val subRouteDataList = withContext(Dispatchers.IO) { getJsonString(tdxUrl, accessToken) }

        // 格式化 subRouteDataList 為字符串
        return formatSubRouteData(subRouteDataList)
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
    private fun getJsonString(url: String, accessToken: String): List<SubRouteData> {
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

            val objectMapper = ObjectMapper()
            val jsonNodes = objectMapper.readTree(jsonString)

            val subRouteDataMap = mutableMapOf<String, Pair<String, String>>()

            jsonNodes.forEach { routeNode ->
                val subRoutesNode = routeNode.get("SubRoutes")
                subRoutesNode.forEach { subRouteNode ->
                    val subRouteID = subRouteNode.get("SubRouteID").asText()
                    val subRouteName = subRouteNode.get("SubRouteName").get("Zh_tw").asText()
                    val headsign = subRouteNode.get("Headsign").asText()
                    subRouteDataMap[subRouteID] = Pair(subRouteName, headsign)
                }
            }

            return subRouteDataMap.values.map { SubRouteData(it.first, it.second) }
        }
    }
    private fun formatSubRouteData(subRouteDataList: List<SubRouteData>): String {
        return subRouteDataList.joinToString(separator = "\n") { subRouteData ->
            "${subRouteData.subRouteName}\n${subRouteData.headsign}\n\n"
        }
    }
}

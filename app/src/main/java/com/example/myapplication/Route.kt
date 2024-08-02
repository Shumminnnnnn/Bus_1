package com.example.myapplication

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.JsonArray
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.GzipSource
import okio.buffer

object Route {
    suspend fun main(): String {
        val tokenUrl = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
        val tdxUrl = "https://tdx.transportdata.tw/api/basic/v2/Bus/StopOfRoute/City/Taoyuan/156?%24top=30&%24format=JSON"
        val clientId = "sherrysweet28605520-0d7e0818-4151-4795" // clientId
        val clientSecret = "797fef62-dd98-4e6f-9af4-7e116f979896" // clientSecret

        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val tokenInfo = withContext(Dispatchers.IO) { getAccessToken(tokenUrl, clientId, clientSecret) }
        val tokenElem: JsonNode = objectMapper.readTree(tokenInfo)
        val accessToken: String = tokenElem.get("access_token").asText()
        return withContext(Dispatchers.IO) { getRouteString(tdxUrl, accessToken) }
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
    private fun getRouteString(url: String, accessToken: String): String {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept-Encoding", "gzip")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body ?: throw IOException("Empty response body")

            val jsonString =
                if ("gzip".equals(response.header("Content-Encoding"), ignoreCase = true)) {
                    responseBody.source().use { source ->
                        GzipSource(source).buffer().use { gzipBuffer ->
                            gzipBuffer.readUtf8()
                        }
                    }
                } else {
                    responseBody.string()
                }

            val gson = Gson()
            val jsonArray = gson.fromJson(jsonString, JsonArray::class.java)
            val stopNames = StringBuilder()
            for (jsonElement in jsonArray) {
                val jsonObject = jsonElement.asJsonObject

                val routeNameZhTw = jsonObject.getAsJsonObject("RouteName").getAsJsonPrimitive("Zh_tw").asString
                stopNames.append("路線名稱: ").append(routeNameZhTw).append("\n\n")

                val direction = jsonObject.getAsJsonPrimitive("Direction").asInt
                val directionStr = if (direction == 0) "去程" else "返程"
                stopNames.append("方向: ").append(directionStr).append("\n")

                val stopsArray = jsonObject.getAsJsonArray("Stops")
                for (stopElement in stopsArray) {
                    val stopObject = stopElement.asJsonObject
                    val stopNameZhTw = stopObject.getAsJsonObject("StopName").getAsJsonPrimitive("Zh_tw").asString
                    stopNames.append("站牌名稱: ").append(stopNameZhTw).append("\n")
                }
                stopNames.append("\n")
            }
            return stopNames.toString()
        }
    }
}
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

object Route_price {
    suspend fun main(): String {
        val tokenUrl = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
        val tdxUrl = "https://tdx.transportdata.tw/api/basic/v2/Bus/RouteFare/City/Taoyuan/${Route_depdes.subRouteName}?%24top=30&%24format=JSON"
        val clientId = "11026349-b9820ce1-cd51-4721" // clientId
        val clientSecret = "c02bf37f-9945-4fcd-bb6d-8a4a2769716c" // clientSecret

        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val tokenInfo = withContext(Dispatchers.IO) { getAccessToken(tokenUrl, clientId, clientSecret) }
        val tokenElem: JsonNode = objectMapper.readTree(tokenInfo)
        val accessToken: String = tokenElem.get("access_token").asText()
        return withContext(Dispatchers.IO) { getJsonString(tdxUrl, accessToken) }
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
    private fun getJsonString(url: String, accessToken: String): String {
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

            val gson = Gson()
            val jsonArray = gson.fromJson(jsonString, JsonArray::class.java)
            val result = StringBuilder()
            val priceCountMap = mutableMapOf<Int, Int>()

            for (jsonElement in jsonArray) {
                val jsonObject = jsonElement.asJsonObject
                val odFares = jsonObject.getAsJsonArray("ODFares")
                for (odFare in odFares) {
                    val fares = odFare.asJsonObject.getAsJsonArray("Fares")
                    for (fare in fares) {
                        val price = fare.asJsonObject.get("Price").asInt
                        priceCountMap[price] = priceCountMap.getOrDefault(price, 0) + 1
                    }
                }
            }

            val maxPrice = priceCountMap.maxByOrNull { it.value }?.key

            if (maxPrice != null) {
                result.append("全程一段票($maxPrice 元)\n")
            }

            return result.toString()
        }
    }
}

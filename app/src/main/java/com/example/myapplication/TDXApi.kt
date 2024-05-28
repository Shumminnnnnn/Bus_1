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

object TDXApi {
    suspend fun main(): String {
        val tokenUrl = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
        val tdxUrl = "https://tdx.transportdata.tw/api/basic/v2/Bus/News/City/Taoyuan?%24top=30&%24format=JSON"
        val clientId = "sherrysweet28605520-0d7e0818-4151-4795" //your clientId
        val clientSecret = "797fef62-dd98-4e6f-9af4-7e116f979896" //your clientSecret

        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val tokenInfo = withContext(Dispatchers.IO) { getAccessToken(tokenUrl, clientId, clientSecret) }
        val tokenElem: JsonNode = objectMapper.readTree(tokenInfo)
        val accessToken: String = tokenElem.get("access_token").asText()
        return withContext(Dispatchers.IO) {getJsonString(tdxUrl, accessToken)}
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
            // Parse JSON and extract titles
            val gson = Gson()
            val jsonArray = gson.fromJson(jsonString, JsonArray::class.java)
            val titles = StringBuilder()
            for (jsonElement in jsonArray) {
                val jsonObject = jsonElement.asJsonObject
                val title = jsonObject.get("Title").asString
                titles.append(title).append("\n\n")
            }
            return titles.toString()

            // Parse JSON and extract Title and Description using Gson
//            val gson = Gson()
//            val jsonArray = JsonParser.parseString(jsonString).asJsonArray
//            val resultArray = JsonArray()
//
//            for (element in jsonArray) {
//                val jsonObject = element.asJsonObject
//                val resultObject = JsonObject()
//                resultObject.addProperty("Title", jsonObject.get("Title").asString)
//                resultObject.addProperty("Description", jsonObject.get("Description").asString)
//                resultArray.add(resultObject)
//            }
//            return gson.toJson(resultArray)
        }
    }
}
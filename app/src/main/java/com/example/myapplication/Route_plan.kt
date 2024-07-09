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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Route_plan {
    private var formattedDate: String = ""
    private var staticTime: String = ""

    init {
        // Get current time and format it once at initialization
        val currentTime = Calendar.getInstance().time
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        formattedDate = dateFormatter.format(currentTime)

        // Compute static time as HH:mm:ss plus 1 minute
        val calendar = Calendar.getInstance()
        calendar.time = currentTime
        calendar.add(Calendar.MINUTE, 1) // Add 1 minute

        val timeFormatterWithMinute = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        staticTime = timeFormatterWithMinute.format(calendar.time)
    }

    suspend fun main(): String {
        return try {
            val tokenUrl = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
            val clientId = "s11026310-7c639d60-e149-4847"
            val clientSecret = "a1e0f98b-ff0c-44bb-80b7-cb9c6ebad7e6"

            val objectMapper = ObjectMapper()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            val tokenInfo = withContext(Dispatchers.IO) { getAccessToken(tokenUrl, clientId, clientSecret) }
            val tokenElem: JsonNode = objectMapper.readTree(tokenInfo)
            val accessToken: String = tokenElem.get("access_token").asText()

            // Construct TDX URL with static formatted time
            val tdxUrl = constructTdxUrl()

            withContext(Dispatchers.IO) { getJsonString(tdxUrl, accessToken) }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun constructTdxUrl(): String {
        // Format time as HH:mm:ss and URL encode it
        val timeFormatted = staticTime.replace(":", "%3A")
        return "https://tdx.transportdata.tw/api/maas/routing?origin=24.957677%2C121.240729&destination=24.953601%2C121.225383&gc=1.0&top=5&transit=5&transfer_time=0%2C60&depart=${formattedDate}T${timeFormatted}&first_mile_mode=0&first_mile_time=15&last_mile_mode=0&last_mile_time=15"
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
                responseBody.source().use { source ->
                    GzipSource(source).buffer().use { gzipBuffer ->
                        gzipBuffer.readUtf8()
                    }
                }
            } else {
                responseBody.string()
            }

            return parseJson(jsonString)
        }
    }

    private fun parseJson(jsonString: String): String {
        val objectMapper = ObjectMapper()
        val rootNode = objectMapper.readTree(jsonString)

        val result = rootNode.get("result").asText()
        if (result != "success") {
            return "Error: ${rootNode.get("message").asText()}"
        }

        val routes = rootNode.path("data").path("routes")
        if (routes.isEmpty) {
            return "No route planning results."
        }

        val sb = StringBuilder()
        for (route in routes) {
            val startTime = route.get("start_time").asText().split("T").getOrNull(1)?.substring(0, 5) ?: "N/A"
            val endTime = route.get("end_time").asText().split("T").getOrNull(1)?.substring(0, 5) ?: "N/A"
            val travelTime = route.get("travel_time").asInt() / 60
            val totalPrice = route.get("total_price").asText()

            sb.append("$startTime - $endTime ")
                .append("($travelTime 分鐘)                       ")
                .append("車資: $totalPrice\n\n")

            val sections = route.path("sections")
            for (section in sections) {
                when (section.get("type").asText()) {
                    "pedestrian" -> {
                        sb.append("步行到 ")
                        val arrivalPlace = section.path("arrival").path("place")
                        if (arrivalPlace.get("type").asText() == "station") {
                            sb.append("${arrivalPlace.get("name").asText()} ")
                        } else {
                            val location = arrivalPlace.path("location")
                            sb.append("lat:${location.get("lat").asDouble()}, lng: ${location.get("lng").asDouble()} ")
                        }
                        val duration = section.path("travelSummary").get("duration").asInt() / 60
                        sb.append("($duration 分鐘)\n\n")
                    }
                    "transit" -> {
                        sb.append("搭乘 ")
                        val transport = section.path("transport")
                        if (transport.get("mode").asText() == "Bus") {
                            sb.append("${transport.get("name").asText()} ")
                        }
                        val duration = section.path("travelSummary").get("duration").asInt() / 60
                        sb.append("($duration 分鐘)\n")
                        val departurePlace = section.path("departure").path("place")
                        val arrivalPlace = section.path("arrival").path("place")
                        sb.append("起點站 > 終點站: ")
                        if (departurePlace.get("type").asText() == "station") {
                            sb.append("${departurePlace.get("name").asText()} > ")
                        }
                        if (arrivalPlace.get("type").asText() == "station") {
                            sb.append("${arrivalPlace.get("name").asText()}\n\n")
                        }
                    }
                }
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}

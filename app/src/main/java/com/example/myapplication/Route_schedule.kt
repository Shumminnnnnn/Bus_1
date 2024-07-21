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

object Route_schedule {
    suspend fun main(): String {
        val tokenUrl = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
        val tdxUrl = "https://tdx.transportdata.tw/api/basic/v2/Bus/Schedule/City/Taoyuan/${Route_depdes.subRouteName}?%24top=30&%24format=JSON"
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
                responseBody.source().use { source ->
                    GzipSource(source).buffer().use { gzipBuffer ->
                        gzipBuffer.readUtf8()
                    }
                }
            } else {
                responseBody.string()
            }

            val objectMapper = ObjectMapper()
            val jsonArray = objectMapper.readTree(jsonString)

            val result = StringBuilder()
            var routeNameAppended = false

            for (route in jsonArray) {
                val routeName = route["RouteName"]["Zh_tw"].asText()
                if (!routeNameAppended) {
                    result.append("路線名稱: ").append(routeName).append("\n\n")
                    routeNameAppended = true
                }

                val timetables = route["Timetables"]
                val direction = route["Direction"].asInt()
                val directionLabel = if (direction == 0) "去程" else "返程"

                if (direction == 1) {
                    result.append("<<DIVIDER>>")
                }
                result.append("方向: ").append(directionLabel).append("\n\n")

                val holidayTimetables = timetables.filter {
                    it["ServiceDay"]["Sunday"].asInt() == 1 || it["ServiceDay"]["Saturday"].asInt() == 1
                }.sortedBy { it["TripID"].asText().replace("-", "").toInt() }

                val weekdayTimetables = timetables.filter {
                    it["ServiceDay"]["Monday"].asInt() == 1 || it["ServiceDay"]["Tuesday"].asInt() == 1 ||
                            it["ServiceDay"]["Wednesday"].asInt() == 1 || it["ServiceDay"]["Thursday"].asInt() == 1 ||
                            it["ServiceDay"]["Friday"].asInt() == 1
                }.sortedBy { it["TripID"].asText().replace("-", "").toInt() }

                if (holidayTimetables.isNotEmpty()) {
                    result.append("假日時刻表: \n")
                    for (timetable in holidayTimetables) {
                        val stopTimes = timetable["StopTimes"]
                        for (stopTime in stopTimes) {
                            val arrivalTime = stopTime["ArrivalTime"].asText()
                            result.append(arrivalTime).append("\n")
                        }
                    }
                    result.append("\n")
                }

                if (weekdayTimetables.isNotEmpty()) {
                    result.append("平日時刻表: \n")
                    for (timetable in weekdayTimetables) {
                        val stopTimes = timetable["StopTimes"]
                        for (stopTime in stopTimes) {
                            val arrivalTime = stopTime["ArrivalTime"].asText()
                            result.append(arrivalTime).append("\n")
                        }
                    }
                    result.append("\n")
                }
            }
            return result.toString()
        }
    }
}

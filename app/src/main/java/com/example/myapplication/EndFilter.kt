package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class EndFilter : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val stopResult = remember { mutableStateOf<String?>(null) }
                    val lastStopResult = remember { mutableStateOf<String?>(null) }
                    var inputText by remember { mutableStateOf("") }
                    var isLoading by remember { mutableStateOf(false) }

                    fun fetchStopData(stopNumber: String) {
                        isLoading = true
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val encodedStopNumber = URLEncoder.encode(stopNumber, StandardCharsets.UTF_8.toString())
                                val url = "https://tdx.transportdata.tw/api/advanced/V3/Map/GeoCode/Coordinate/Markname/$encodedStopNumber?%24format=JSON"
                                val stopResultJson = Plan_filter.main(url) // Pass dynamic URL
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    if (stopResultJson.isNotBlank()) {
                                        stopResult.value = stopResultJson
                                        lastStopResult.value = stopResultJson // Update last result
                                    } else {
                                        stopResult.value = "查無此地點資料，請重新輸入地點"
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("StopFilter", "Error fetching stop data: ${e.message}", e)
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    stopResult.value = "Error fetching stop data: ${e.message}"
                                    // Retain previous result on error
                                    lastStopResult.value = lastStopResult.value ?: stopResult.value
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { newValue ->
                                    inputText = newValue
                                    if (newValue.isNotBlank()) {
                                        fetchStopData(newValue)
                                    } else {
                                        stopResult.value = lastStopResult.value // 如果搜尋欄位內容清空，仍保留上一次的回傳內容
                                    }
                                },
                                label = { Text("請輸入終點") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                singleLine = true // Ensures that input field remains on one line
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // 如果结果不为空则显示
                            stopResult.value?.let { result ->
                                result.split("\n\n").forEachIndexed { index, stopItem ->
                                    if (stopItem.isNotEmpty()) {
                                        Text(
                                            text = stopItem,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            fontSize = 16.sp
                                        )
                                        if (index < result.split("\n\n").size - 1) {
                                            Divider(
                                                color = Color.Gray,
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(200.dp)) // 回传内容底部和键盘之间的距离，避免被遮挡
                            }
                        }
                    }
                }
            }
        }
    }
}

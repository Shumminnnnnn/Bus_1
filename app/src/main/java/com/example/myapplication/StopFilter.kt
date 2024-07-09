package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class StopFilter : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val stopResults = remember { mutableStateOf<List<StopInfo>?>(null) }
                    var inputText by remember { mutableStateOf("") }

                    fun fetchStopData(stopNumber: String) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val encodedStopNumber = URLEncoder.encode(stopNumber, StandardCharsets.UTF_8.toString())
                                val url = "https://tdx.transportdata.tw/api/advanced/V3/Map/GeoCode/Coordinate/Markname/$encodedStopNumber?%24format=JSON"
                                val stopResultList = Stop_filter.main(url)
                                withContext(Dispatchers.Main) {
                                    stopResults.value = stopResultList
                                }
                            } catch (e: Exception) {
                                stopResults.value = null
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
                                        stopResults.value = null
                                    }
                                },
                                label = { Text("想搜尋哪個地點呢?") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            stopResults.value?.let { resultList ->
                                resultList.forEach { stopInfo ->
                                    Text(
                                        text = "${stopInfo.markname}\n",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .clickable {
                                                val intent = Intent(this@StopFilter, StopActivity::class.java).apply {
                                                    putExtra("latitude", stopInfo.formattedLatitude.toDouble())
                                                    putExtra("longitude", stopInfo.formattedLongitude.toDouble())
                                                }
                                                startActivity(intent)
                                            },
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            } ?: run {
                                Text(
                                    text = "查無此地點資料，請重新輸入地點",
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

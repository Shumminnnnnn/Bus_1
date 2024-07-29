package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
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
                    var inputText by remember { mutableStateOf(TextFieldValue("")) }

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
                                withContext(Dispatchers.Main) {
                                    stopResults.value = emptyList()
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top purple area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF9e7cfe))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { finish() },
                                        modifier = Modifier
                                            .background(Color(0xFF9e7cfe))
                                            .size(40.dp)
                                            .offset(x = (-8).dp) // Move the button to the left
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(20.dp))
                                            .height(48.dp)
                                            .background(Color.White)
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (inputText.text.isEmpty()) {
                                            Text(
                                                text = "想搜尋哪個地點呢?",
                                                color = Color.Gray,
                                                fontSize = 16.sp
                                            )
                                        }
                                        BasicTextField(
                                            value = inputText,
                                            onValueChange = { newValue ->
                                                inputText = newValue
                                                if (newValue.text.isNotBlank()) {
                                                    fetchStopData(newValue.text)
                                                } else {
                                                    stopResults.value = null
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            textStyle = TextStyle(
                                                fontSize = 16.sp,
                                                textAlign = TextAlign.Start,
                                                color = Color.Black
                                            ),
                                            singleLine = true
                                        )
                                    }
                                }
                            }

                            // Scrollable content area
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(8.dp)
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "搜尋結果",
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                )
                                if (inputText.text.isNotBlank()) {
                                    stopResults.value?.let { resultList ->
                                        if (resultList.isEmpty()) {
                                            Text(
                                                text = "查無此地點資料，請重新輸入地點",
                                                modifier = Modifier.padding(8.dp),
                                                fontSize = 16.sp
                                            )
                                        } else {
                                            resultList.forEach { stopInfo ->
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.clickable {
                                                            val intent = Intent(this@StopFilter, StopActivity::class.java).apply {
                                                                putExtra("markname", stopInfo.markname)  // Passing markname
                                                                putExtra("latitude", stopInfo.formattedLatitude.toDouble())
                                                                putExtra("longitude", stopInfo.formattedLongitude.toDouble())
                                                            }
                                                            startActivity(intent)
                                                        }
                                                    ) {
                                                        Image(
                                                            painter = painterResource(id = R.drawable.map_pin),
                                                            contentDescription = null,
                                                            colorFilter = ColorFilter.tint(Color(0xFF9e7cfe)), // Set the tint color to purple
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .padding(end = 8.dp)
                                                        )
                                                        Text(
                                                            text = stopInfo.markname,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontSize = 16.sp
                                                        )
                                                    }
                                                    Divider(
                                                        color = Color.Gray,
                                                        thickness = 1.dp,
                                                        modifier = Modifier
                                                            .padding(vertical = 8.dp)
                                                            .width(300.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Bottom purple area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF9e7cfe))
                                    .padding(25.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

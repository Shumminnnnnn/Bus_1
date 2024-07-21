package com.example.myapplication

import android.content.Intent
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RouteFilter : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val routeResult = remember { mutableStateOf("") }
                    var inputText by remember { mutableStateOf("") }

                    fun fetchRouteData(routeNumber: String) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val routeResultJson = Route_filter.main(routeNumber)
                                withContext(Dispatchers.Main) {
                                    routeResult.value = routeResultJson
                                }
                            } catch (e: Exception) {
                                Log.e("RouteFilter", "Error fetching route data: ${e.message}", e)
                                withContext(Dispatchers.Main) {
                                    routeResult.value = "Error fetching route data: ${e.message}"
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
                                onValueChange = {},
                                label = { Text("今天想搭哪輛公車呢?") },
                                enabled = false,
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            routeResult.value.split("\n").chunked(2).forEach { routeItem ->
                                if (routeItem.size == 2) {
                                    val (subRouteName, headsign) = routeItem
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .border(1.dp, Color.Gray)
                                            .padding(8.dp)
                                            .clickable {
                                                Route_depdes.subRouteName = subRouteName
                                                val intent = Intent(this@RouteFilter, RouteActivity4::class.java)
                                                intent.putExtra("subRouteName", subRouteName)
                                                startActivity(intent)
                                                Log.d("RouteFilter", "Navigating to RouteActivity4 with: $subRouteName")
                                            },
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Column {
                                            Text(
                                                text = subRouteName,
                                                fontSize = 16.sp,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = headsign,
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(200.dp))
                        }

                        CustomKeyboard(
                            onKeyPress = { key ->
                                when (key) {
                                    "清除" -> {
                                        inputText = ""
                                        routeResult.value = ""
                                    }
                                    else -> {
                                        inputText += key
                                        fetchRouteData(inputText)
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .zIndex(1f) // 確保鍵盤在其他內容上
                                .background(Color(0xFF6650a4))
                                .height(200.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomKeyboard(onKeyPress: (String) -> Unit, modifier: Modifier = Modifier) {
    val keys = listOf(
        listOf("L", "1", "2", "3"),
        listOf("GR", "4", "5", "6"),
        listOf("BR", "7", "8", "9"),
        listOf("A", "B", "0", "清除")
    )

    Column(modifier = modifier.padding(8.dp).background(Color(0xFF6650a4))) {
        keys.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Button(
                        onClick = {
                            onKeyPress(key)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White, // 鍵盤按鈕顏色
                            contentColor = Color.Black // 鍵盤按鈕內文字顏色
                        ),
                        modifier = Modifier
                            .padding(2.dp)
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text(text = key, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

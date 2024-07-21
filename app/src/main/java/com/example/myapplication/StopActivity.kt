package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.myapplication.ui.theme.MyApplicationTheme

class StopActivity : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val routeResult = remember { mutableStateOf("載入中...") }
                    val currentLocation = remember { mutableStateOf("所在位置:") }
                    val showRouteResult = remember { mutableStateOf(false) }

                    val latitude = intent.getDoubleExtra("latitude", 0.0)
                    val longitude = intent.getDoubleExtra("longitude", 0.0)

                    currentLocation.value = "所在位置: $latitude, $longitude"

                    LaunchedEffect(latitude, longitude) {
                        try {
                            val routeResultJson = ArroundStop.fetchStopData(latitude, longitude)
                            routeResult.value = routeResultJson
                        } catch (e: Exception) {
                            Log.e("StopActivity", "Error fetching route data: ${e.message}", e)
                            routeResult.value = "Error fetching route data: ${e.message}"
                        }
                    }

                    ScrollableContent7(
                        routeResult = routeResult.value,
                        currentLocation = currentLocation.value,
                        showRouteResult = showRouteResult.value,
                        onLocationClick = {
                            val intent = Intent(this@StopActivity, StopFilter::class.java)
                            startActivity(intent)
                        },
                        onQueryClick = {
                            showRouteResult.value = true
                        },
                        onCurrentLocationClick = {
                            val intent = Intent(this@StopActivity, MapActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollableContent7(
    routeResult: String,
    currentLocation: String,
    showRouteResult: Boolean,
    onLocationClick: () -> Unit,
    onQueryClick: () -> Unit,
    onCurrentLocationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Use Row to place "所在位置" and "目前位置" button horizontally
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(1.dp, Color.Gray)
        ) {
            // Create a clickable Box for "所在位置" text
            Box(
                modifier = Modifier
                    .weight(1f)  // Take up remaining space
                    .clickable(onClick = onLocationClick)
                    .padding(8.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = currentLocation,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Place the "目前位置" button to the right
            Button(
                onClick = onCurrentLocationClick,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text("目前位置")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onQueryClick) {
            Text("查詢")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showRouteResult) {
            if (routeResult == "載入中...") {
                Text(text = routeResult, modifier = Modifier.padding(16.dp))
            } else {
                routeResult.split("\n\n").forEach { routeItem ->
                    if (routeItem.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .border(1.dp, Color.Gray)
                                .padding(8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                            ) {
                                routeItem.split("\n").forEach { line ->
                                    Text(text = line)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

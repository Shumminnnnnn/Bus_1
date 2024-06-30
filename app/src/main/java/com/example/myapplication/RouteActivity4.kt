package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.*

class RouteActivity4 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                RouteActivityContent()
            }
        }
    }

    @Composable
    fun RouteActivityContent() {
        val routeInfo = remember { mutableStateOf<RouteInfo?>(null) }
        val currentDirection = remember { mutableStateOf(0) }

        val coroutineScope = rememberCoroutineScope()

        // Effect to launch coroutine for fetching data every 30 seconds
        LaunchedEffect(true) {
            // Initially fetch data
            fetchDataAndUpdate(routeInfo)

            // Set up periodic data refresh
            while (isActive) {
                delay(20000) // Wait for 20 seconds
                fetchDataAndUpdate(routeInfo)
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ScrollableContent5(
                routeInfo = routeInfo.value,
                currentDirection = currentDirection.value,
                onButtonClick = { newDirection -> currentDirection.value = newDirection },
                onNavigate1 = {
                    val intent = Intent(this@RouteActivity4, RouteActivity5::class.java)
                    startActivity(intent)
                } ,
                onNavigate2 = {
                    val intent = Intent(this@RouteActivity4, RouteActivity::class.java)
                    startActivity(intent)
                },
                onNavigate3 = {
                    val intent = Intent(this@RouteActivity4, RouteActivity3::class.java)
                    startActivity(intent)
                }

            )
        }
    }override fun onBackPressed() {
        super.onBackPressed()
        // 導向主畫面
        finish()
    }
    private suspend fun fetchDataAndUpdate(routeInfo: MutableState<RouteInfo?>) {
        try {
            val routeResult = Route_arrivetime.main()
            routeInfo.value = routeResult
        } catch (e: Exception) {
            Log.e("RouteActivity4", "Error fetching route data: ${e.message}", e)
        }
    }
}
@Composable
fun ScrollableContent5(
    routeInfo: RouteInfo?,
    currentDirection: Int,
    onButtonClick: (Int) -> Unit,
    onNavigate1: () -> Unit,
    onNavigate2: () -> Unit,
    onNavigate3: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (routeInfo != null) {
            val arrivalTimeInfo = if (currentDirection == 0) routeInfo.arrivalTimeInfoDirection0 else routeInfo.arrivalTimeInfoDirection1

            if (currentDirection == 0) {
                val destinationStopNameZh = routeInfo.routeDepDesInfo.split("\n")[1].split(":")[1].trim()
                Text(text = "往 $destinationStopNameZh\n")
                Text(text = arrivalTimeInfo)
                Button(
                    onClick = { onButtonClick(1) },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // 在Direction等於0時，按鈕顯示routeDepDesInfo中的destinationStopNameZh
                    val departureStopNameZh = routeInfo.routeDepDesInfo.split("\n")[0].split(":")[1].trim()
                    Text(text = "往 $departureStopNameZh")
                }
            } else {
                val departureStopNameZh = routeInfo.routeDepDesInfo.split("\n")[0].split(":")[1].trim()
                Text(text = "往 $departureStopNameZh\n")
                Text(text = arrivalTimeInfo)
                Button(
                    onClick = { onButtonClick(0) },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // 在Direction等於1時，按鈕顯示routeDepDesInfo中的departureStopNameZh
                    val destinationStopNameZh = routeInfo.routeDepDesInfo.split("\n")[1].split(":")[1].trim()
                    Text(text = "往 $destinationStopNameZh")
                }
            }
        } else {
            Text(text = "Loading route data...")
        }

        Button(
            onClick = onNavigate1,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "路線簡圖")
        }
        Button(
            onClick = onNavigate2,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "路線票價")
        }
        Button(
            onClick = onNavigate3,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "路線時刻表")
        }
    }
}
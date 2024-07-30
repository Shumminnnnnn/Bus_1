package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.*

class RouteActivity4 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val subRouteName = intent.getStringExtra("subRouteName") ?: "DefaultSubRouteName"

        setContent {
            MyApplicationTheme {
                RouteActivityContent(subRouteName)
            }
        }
    }

    @Composable
    fun RouteActivityContent(subRouteName: String) {
        val routeInfo = remember { mutableStateOf<RouteInfo?>(null) }
        val currentDirection = remember { mutableStateOf(0) }

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(true) {
            fetchDataAndUpdate(routeInfo, subRouteName)
            while (isActive) {
                delay(20000) // Wait for 20 seconds
                fetchDataAndUpdate(routeInfo, subRouteName)
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                PurpleHeader(
                    onNavigate1 = {
                        val intent = Intent(this@RouteActivity4, RouteActivity5::class.java)
                        startActivity(intent)
                    },
                    onNavigate2 = {
                        val intent = Intent(this@RouteActivity4, RouteActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigate3 = {
                        val intent = Intent(this@RouteActivity4, RouteActivity3::class.java)
                        startActivity(intent)
                    }
                )

                ScrollableContent5(
                    routeInfo = routeInfo.value,
                    currentDirection = currentDirection.value,
                    onButtonClick = { newDirection -> currentDirection.value = newDirection }
                )
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private suspend fun fetchDataAndUpdate(routeInfo: MutableState<RouteInfo?>, subRouteName: String) {
        try {
            val routeResult = Route_arrivetime.main(subRouteName)
            routeInfo.value = routeResult
        } catch (e: Exception) {
            Log.e("RouteActivity4", "Error fetching route data: ${e.message}", e)
        }
    }
}

@Composable
fun PurpleHeader(
    onNavigate1: () -> Unit,
    onNavigate2: () -> Unit,
    onNavigate3: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF9e7cfe))
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            CustomButton(onClick = onNavigate1, icon = painterResource(id = R.drawable.map_pin))
            CustomButton(onClick = onNavigate2, icon = painterResource(id = R.drawable.money))
            CustomButton(onClick = onNavigate3, icon = painterResource(id = R.drawable.info))
        }
    }
}

@Composable
fun ScrollableContent5(
    routeInfo: RouteInfo?,
    currentDirection: Int,
    onButtonClick: (Int) -> Unit
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
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe))
                ) {
                    val departureStopNameZh = routeInfo.routeDepDesInfo.split("\n")[0].split(":")[1].trim()
                    Text(text = "往 $departureStopNameZh")
                }
            } else {
                val departureStopNameZh = routeInfo.routeDepDesInfo.split("\n")[0].split(":")[1].trim()
                Text(text = "往 $departureStopNameZh\n")
                Text(text = arrivalTimeInfo)
                Button(
                    onClick = { onButtonClick(0) },
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe))
                ) {
                    val destinationStopNameZh = routeInfo.routeDepDesInfo.split("\n")[1].split(":")[1].trim()
                    Text(text = "往 $destinationStopNameZh")
                }
            }
        } else {
            Text(text = "載入公車路線動態中...")
        }
    }
}

@Composable
fun CustomButton(onClick: () -> Unit, icon: Painter) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe))
    ) {
        Icon(painter = icon, contentDescription = null,modifier = Modifier
            .size(18.dp))
    }
}

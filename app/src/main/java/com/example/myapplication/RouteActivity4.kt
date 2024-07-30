package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    subRouteName = subRouteName,
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
                    },
                    onBackPress = {
                        onBackPressedDispatcher.onBackPressed()
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
    subRouteName: String,
    onNavigate1: () -> Unit,
    onNavigate2: () -> Unit,
    onNavigate3: () -> Unit,
    onBackPress: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF9e7cfe))
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPress,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                            .offset(x = (-10).dp)
                    )
                }
                Text(
                    text = subRouteName,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                        .offset(x = (-10).dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp), // 调整按钮之间的间距
                modifier = Modifier.padding(end = 16.dp)
            ) {
                CustomButton(onClick = onNavigate1, icon = painterResource(id = R.drawable.baseline_approval_24), offsetX = 70.dp)
                CustomButton(onClick = onNavigate2, icon = painterResource(id = R.drawable.money), offsetX = 40.dp)
                CustomButton(onClick = onNavigate3, icon = painterResource(id = R.drawable.baseline_info_24), offsetX = 10.dp)
            }
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (directionText, buttonText) = if (currentDirection == 0) {
                    val destinationStopNameZh = routeInfo.routeDepDesInfo.split("\n")[1].split(":")[1].trim()
                    val departureStopNameZh = routeInfo.routeDepDesInfo.split("\n")[0].split(":")[1].trim()
                    "往 $destinationStopNameZh" to "往 $departureStopNameZh"
                } else {
                    val departureStopNameZh = routeInfo.routeDepDesInfo.split("\n")[0].split(":")[1].trim()
                    val destinationStopNameZh = routeInfo.routeDepDesInfo.split("\n")[1].split(":")[1].trim()
                    "往 $departureStopNameZh" to "往 $destinationStopNameZh"
                }

                Text(text = directionText)
                Button(
                    onClick = { onButtonClick(if (currentDirection == 0) 1 else 0) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe))
                ) {
                    Text(text = buttonText)
                }
            }

            Text(text = arrivalTimeInfo)
        } else {
            Text(text = "載入公車路線動態中...")
        }
    }
}

@Composable
fun CustomButton(onClick: () -> Unit, icon: Painter, offsetX: Dp) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(48.dp) // 确保按钮是一个正方形，以便变成圆形
            .offset(x = offsetX),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe)),
        contentPadding = PaddingValues(0.dp) // 移除默认的内边距
    ) {
        Icon(painter = icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.White) // 确保图标大小适中且颜色对比度足够
    }
}

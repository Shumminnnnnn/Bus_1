package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
        val isLoading = remember { mutableStateOf(true) }
        val currentDirection = remember { mutableStateOf(0) }

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(true) {
            fetchDataAndUpdate(routeInfo, subRouteName, isLoading)
            while (isActive) {
                delay(15000) // 每15秒更新一次
                fetchDataAndUpdate(routeInfo, subRouteName, isLoading)
                Toast.makeText(this@RouteActivity4, "已更新!", Toast.LENGTH_SHORT).show()
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

    private suspend fun fetchDataAndUpdate(routeInfo: MutableState<RouteInfo?>, subRouteName: String, isLoading: MutableState<Boolean>) {
        isLoading.value = true
        try {
            val routeResult = Route_arrivetime.main(subRouteName)
            routeInfo.value = routeResult
        } catch (e: Exception) {
            Log.e("RouteActivity4", "Error fetching route data: ${e.message}", e)
        } finally {
            isLoading.value = false
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
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

            routeInfo.arrivalTimeInfoDirection0.split("\n").forEach { info ->
                if (info.contains("站名")) {
                    Text(
                        text = info.split(":")[1].trim(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            ArrivalTimeInfoText(arrivalTimeInfo)
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(89.dp)
                            .offset(y = (30).dp)
                            .offset(x = (60).dp)
                            .background(Color(0xFF9e7cfe), shape = CircleShape)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(250.dp)
                            .offset(y = (80).dp)
                            .offset(x = (60).dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "載入公車路線動態中...",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 18.sp,
                            color = Color.Black,
                        ),
                        modifier = Modifier
                            .padding(10.dp)
                            .offset(y = (-45).dp)
                            .offset(x = (65).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ArrivalTimeInfoText(arrivalTimeInfo: String) {
    Column {
        arrivalTimeInfo.split("\n").forEach { info ->
            val color = when {
                info.contains("進站中") || info.contains("即將進站") -> Color(0xFFff4b4b)
                else -> Color.LightGray
            }
            val textColor = Color.Black

            val infoParts = info.split(" ", limit = 2)
            val timeInfo = infoParts[0]
            val stopName = if (infoParts.size > 1) infoParts[1] else ""

            if (stopName.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .background(Color.Transparent)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(40.dp)
                                .background(color, shape = RoundedCornerShape(20.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = timeInfo,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stopName,
                            color = textColor,
                            fontSize = 16.sp
                        )
                    }
                }
            }
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
            .size(48.dp)
            .offset(x = offsetX),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(painter = icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.White) // 确保图标大小适中且颜色对比度足够
    }
}

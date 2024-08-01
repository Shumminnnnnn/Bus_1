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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

class StopActivity : ComponentActivity() {
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
                    val markname = intent.getStringExtra("markname") ?: ""

                    currentLocation.value = " $markname"

                    LaunchedEffect(latitude, longitude) {
                        try {
                            val routeResultJson = ArroundStop.fetchStopData(latitude, longitude)
                            routeResult.value = routeResultJson
                        } catch (e: Exception) {
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
                    ) { onBackClick() }
                }
            }
        }
    }

    private fun onBackClick() {
        finish()
    }
}

@Composable
fun ScrollableContent7(
    routeResult: String,
    currentLocation: String,
    showRouteResult: Boolean,
    onLocationClick: () -> Unit,
    onQueryClick: () -> Unit,
    onCurrentLocationClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top purple area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF9e7cfe))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick,
                            modifier = Modifier.offset(x = (-13).dp)
                                .offset(y = 3.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "附近站牌",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(8f)
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                                .height(60.dp)
                                .clickable(onClick = onLocationClick),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(style = SpanStyle(color = Color.Gray)) {
                                            append("所在位置: ")
                                        }
                                        withStyle(style = SpanStyle(color = Color.Black)) {
                                            append(currentLocation)
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            IconButton(
                                onClick = onCurrentLocationClick,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.baseline_location_on_24),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(Color(0xFF9e7cfe)),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onQueryClick,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (showRouteResult) {
                    if (routeResult == "載入中...") {
                        Text(text = routeResult, modifier = Modifier.padding(10.dp))
                    } else {
                        val routeItems = routeResult.split("\n\n")
                        routeItems.forEachIndexed { index, routeItem ->
                            if (routeItem.isNotEmpty()) {
                                if (index == 0) {
                                    Spacer(modifier = Modifier.height(8.dp)) // Add vertical space before the first item
                                }
                                val lines = routeItem.split("\n")
                                val stationName = lines.firstOrNull() ?: ""
                                val routes = lines.drop(1)

                                if (stationName.contains("500公尺內無公車站牌")) {
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
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "附近500公尺內無站牌!",
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
                                } else {
                                    val boxModifier = Modifier
                                        .fillMaxWidth(0.95f)
                                        .padding(horizontal = 8.dp)

                                    Box(
                                        modifier = boxModifier
                                            .fillMaxWidth()
                                            .padding(15.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                        ) {
                                            // Add the icon only to the station name
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.baseline_location_on_24),
                                                    contentDescription = null,
                                                    colorFilter = ColorFilter.tint(Color(0xFF9e7cfe)),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = stationName,
                                                    style = androidx.compose.ui.text.TextStyle(
                                                        fontSize = 16.sp,
                                                        color = Color.Black,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                            routes.forEach { line ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.logo),
                                                        contentDescription = null,
                                                        colorFilter = ColorFilter.tint(Color(0xFF9e7cfe)),
                                                        modifier = Modifier
                                                            .size(80.dp)
                                                            .offset(x = (-26).dp, y = 10.dp) // 调整 logo 的位置
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .offset(x = (-40).dp, y = (-5).dp) // 根据需要调整文字的位置
                                                    ) {
                                                        Text(
                                                            text = line,
                                                            style = androidx.compose.ui.text.TextStyle(
                                                                fontSize = 16.sp,
                                                                color = Color.Black
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (index != routeItems.size - 2) {
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 0.dp, y = (-15).dp) // 根据需要调整文字的位置
                                        ){
                                            Divider(
                                                color = Color(0xFF9e7cfe),
                                                thickness = 2.dp,
                                                modifier = Modifier.padding(horizontal = 5.dp)
                                                    .fillMaxWidth(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                                    .offset(y = (10).dp)
                                    .offset(x = (58).dp)
                                    .background(Color(0xFF9e7cfe), shape = CircleShape)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(250.dp)
                                    .offset(y = (60).dp)
                                    .offset(x = (58).dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "附近站牌搜尋",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 18.sp,
                                color = Color.Black,
                            ),
                            modifier = Modifier
                                .padding(10.dp)
                                .offset(y = (-80).dp)
                                .offset(x = (58).dp)
                        )
                        Text(
                            text = "請輸入所在位置或點擊定位按紐!",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                                color = Color.Gray,
                            ),
                            modifier = Modifier
                                .offset(y = (-80).dp)
                                .offset(x = (58).dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom purple area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFF9e7cfe))
            )
        }
    }
}

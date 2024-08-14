package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RouteActivity3 : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val routeResult = remember { mutableStateOf("載入路線發車時刻表中...") }

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val routeResultJson = Route_schedule.main()
                            withContext(Dispatchers.Main) {
                                routeResult.value = routeResultJson
                            }
                        } catch (e: Exception) {
                            Log.e("RouteActivity3", "Error fetching route data: ${e.message}", e)
                            withContext(Dispatchers.Main) {
                                routeResult.value = "Error fetching route data: ${e.message}"
                            }
                        }
                    }

                    RouteScheduleScreen(routeResult.value) { onBackClick() }
                }
            }
        }
    }

    private fun onBackClick() {
        finish()
    }
}

@Composable
fun RouteScheduleScreen(routeResult: String, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
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
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "公車時刻表",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                val parts = routeResult.split("<<DIVIDER>>")
                val weekdayParts = mutableMapOf<String, String>()
                val holidayParts = mutableMapOf<String, String>()

                for (part in parts) {
                    val lines = part.split("\n")
                    val weekdayLines = mutableListOf<String>()
                    val holidayLines = mutableListOf<String>()
                    var isWeekday = false
                    var isHoliday = false
                    var directionLabel = ""

                    for (line in lines) {
                        when {
                            line.contains("平日時刻表") -> {
                                isWeekday = true
                                isHoliday = false
                            }
                            line.contains("假日時刻表") -> {
                                isWeekday = false
                                isHoliday = true
                            }
                            line.contains("方向: 去程") -> {
                                directionLabel = "去程"
                            }
                            line.contains("方向: 返程") -> {
                                directionLabel = "返程"
                            }
                            else -> {
                                if (isWeekday) {
                                    weekdayLines.add(line)
                                } else if (isHoliday) {
                                    holidayLines.add(line)
                                }
                            }
                        }
                    }

                    if (weekdayLines.isNotEmpty()) {
                        weekdayParts[directionLabel] = weekdayLines.joinToString("\n")
                    }

                    if (holidayLines.isNotEmpty()) {
                        holidayParts[directionLabel] = holidayLines.joinToString("\n")
                    }
                }

                if (holidayParts.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFbaa2ff))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "假日時刻表",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        holidayParts.forEach { (direction, part) ->
                            Column(
                                modifier = Modifier.weight(1f).padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFd6c9fc))
                                        .clip(RoundedCornerShape(20.dp))
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = direction,
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.White
                                        )
                                    )
                                }
                                Text(
                                    text = part,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                if (weekdayParts.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFbaa2ff))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "平日時刻表",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        weekdayParts.forEach { (direction, part) ->
                            Column(
                                modifier = Modifier.weight(1f).padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFd6c9fc))
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = direction,
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.White
                                        )
                                    )
                                }
                                Text(
                                    text = part,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF9e7cfe))
                .padding(16.dp)
        ) {
        }
    }
}

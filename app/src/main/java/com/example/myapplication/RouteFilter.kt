package com.example.myapplication

import android.content.Intent
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RouteFilter : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
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
                    var placeholderVisible by remember { mutableStateOf(true) }
                    val coroutineScope = rememberCoroutineScope()

                    fun fetchRouteData(routeNumber: String) {
                        coroutineScope.launch {
                            try {
                                val routeResultJson = withContext(Dispatchers.IO) {
                                    Route_filter.main(routeNumber)
                                }
                                routeResult.value = routeResultJson
                            } catch (e: Exception) {
                                Log.e("RouteFilter", "Error fetching route data: ${e.message}", e)
                                routeResult.value = "Error fetching route data: ${e.message}"
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 245.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.height(125.dp))

                            if (inputText.isEmpty()) {
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
                                                .offset(x = (60).dp)
                                                .background(Color(0xFF9e7cfe), shape = CircleShape)
                                        )
                                        Image(
                                            painter = painterResource(id = R.drawable.logo),
                                            contentDescription = "Logo",
                                            modifier = Modifier.size(250.dp)
                                                .offset(y = (60).dp)
                                                .offset(x = (60).dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "公車路線搜尋",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 18.sp,
                                            color = Color.Black,
                                        ),
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .offset(y = (-80).dp)
                                            .offset(x = (50).dp)
                                    )
                                    Text(
                                        text = "請輸入公車路線編號!",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 15.sp,
                                            color = Color.Gray,
                                        ),
                                        modifier = Modifier
                                            .offset(y = (-80).dp)
                                            .offset(x = (50).dp)
                                    )
                                }
                            } else {
                                val routeItems = routeResult.value.split("\n").chunked(2)
                                routeItems.forEachIndexed { index, routeItem ->
                                    if (routeItem.size == 2) {
                                        val (subRouteName, headsign) = routeItem

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    Route_depdes.subRouteName = subRouteName
                                                    val intent = Intent(
                                                        this@RouteFilter,
                                                        RouteActivity4::class.java
                                                    )
                                                    intent.putExtra("subRouteName", subRouteName)
                                                    startActivity(intent)
                                                    Log.d(
                                                        "RouteFilter",
                                                        "Navigating to RouteActivity4 with: $subRouteName"
                                                    )
                                                }
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.size(80.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(35.dp)
                                                        .offset(x = 0.dp)
                                                        .background(
                                                            Color(0xFF9e7cfe),
                                                            shape = CircleShape
                                                        )
                                                )
                                                Image(
                                                    painter = painterResource(id = R.drawable.logo),
                                                    contentDescription = "Logo",
                                                    modifier = Modifier
                                                        .size(90.dp)
                                                        .offset(y = (-81).dp)
                                                        .offset(x = 0.dp)
                                                )
                                            }
                                            Column(
                                                modifier = Modifier.padding(start = 8.dp)
                                            ) {
                                                Text(
                                                    text = subRouteName,
                                                    fontSize = 20.sp,
                                                    color = Color.Black,
                                                )
                                                Text(
                                                    text = headsign,
                                                    fontSize = 18.sp,
                                                    color = Color.Gray,
                                                )
                                            }
                                        }

                                        if (index < routeItems.size - 1) {
                                            Divider(color = Color.LightGray, thickness = 1.dp)
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .background(Color(0xFF9e7cfe))
                                .align(Alignment.BottomCenter)
                        )

                        CustomKeyboard(
                            onKeyPress = { key ->
                                when (key) {
                                    "清除" -> {
                                        inputText = ""
                                        routeResult.value = ""
                                        placeholderVisible = true
                                    }

                                    else -> {
                                        inputText += key
                                        fetchRouteData(inputText)
                                        placeholderVisible = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 20.dp)
                                .zIndex(1f)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(125.dp)
                                .background(Color(0xFF9e7cfe))
                                .align(Alignment.TopCenter)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(
                                        start = 8.dp,
                                        end = 8.dp,
                                        top = 8.dp
                                    )
                                ) {
                                    IconButton(
                                        onClick = { finish() },
                                        modifier = Modifier
                                            .background(Color(0xFF9e7cfe))
                                            .offset(x = (-90).dp)
                                            .offset(y = 3.dp)

                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                            contentDescription = "Back",
                                            tint = Color.White,
                                        )
                                    }
                                    Text(
                                        text = "  路線查詢",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        modifier = Modifier
                                            .offset(x = (-90).dp)
                                    )
                                }

                                TextField(
                                    value = inputText,
                                    onValueChange = {},
                                    placeholder = { if (placeholderVisible) Text("今天想搭哪輛公車呢?") else null },
                                    readOnly = true,
                                    colors = TextFieldDefaults.textFieldColors(
                                        disabledTextColor = Color.Black,
                                        containerColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(30.dp))
                                )
                            }
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

        Column(modifier = modifier.padding(8.dp).background(Color.White)) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    row.forEach { key ->
                        Button(
                            onClick = {
                                onKeyPress(key)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFd6c9fc),
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .padding(2.dp)
                                .weight(1f)
                        ) {
                            Text(text = key)
                        }
                    }
                }
            }
        }
    }
}

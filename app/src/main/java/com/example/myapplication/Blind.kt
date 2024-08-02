package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Blind : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tdxResult = remember { mutableStateOf("載入主畫面中...") }

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val tdxResultJson = TDXApi.main()
                            withContext(Dispatchers.Main) {
                                tdxResult.value = tdxResultJson
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error fetching TDX data: ${e.message}", e)
                            withContext(Dispatchers.Main) {
                                tdxResult.value = "Error fetching TDX data: ${e.message}"
                            }
                        }
                    }

                    BlindContent(
                        onButtonClick1 = {
                            val intent = Intent(this@Blind, NewsActivity::class.java)
                            startActivity(intent)
                        },
                        onButtonClick2 = {
                            val intent = Intent(this@Blind, RouteFilter::class.java)
                            startActivity(intent)
                        },
                        onButtonClick3 = {
                            val intent = Intent(this@Blind, StopActivity::class.java)
                            startActivity(intent)
                        },
                        onButtonClick7 = {
                            val intent = Intent(this@Blind, PlanFilter::class.java)
                            startActivity(intent)
                        },
                        onButtonClickSetting = {
                            val intent = Intent(this@Blind, Setting::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BlindContent(
    onButtonClick1: () -> Unit,
    onButtonClick2: () -> Unit,
    onButtonClick3: () -> Unit,
    onButtonClick7: () -> Unit,
    onButtonClickSetting: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top purple area with text
        Box(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .background(Color(0xFF9e7cfe)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "桃園 der bus",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        // Additional purple area with button
        Box(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .background(Color(0xFFbaa2ff)),
            contentAlignment = Alignment.CenterStart
        ) {
            Button(
                onClick = onButtonClick1,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe)),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .height(38.dp)
                    .width(120.dp)
            ) {
                Text(text = "最新消息")
            }
        }

        // Scrollable content area with centered buttons
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF9e7cfe)), // Apply background color after clip
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onButtonClick2,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp), // Clip button to match box
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = "Search Icon",
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "路線查詢",
                            style = TextStyle(
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF9e7cfe)), // Apply background color after clip
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onButtonClick3,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp), // Clip button to match box
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.stop),
                            contentDescription = "Stop Icon",
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "附近站牌",
                            style = TextStyle(
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF9e7cfe)), // Apply background color after clip
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onButtonClick7,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp), // Clip button to match box
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.plan),
                            contentDescription = "Plan Icon",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "路線規劃",
                            style = TextStyle(
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF9e7cfe)), // Apply background color after clip
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onButtonClickSetting,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp), // Clip button to match box
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.setting),
                            contentDescription = "Setting Icon",
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "設定",
                            style = TextStyle(
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Bottom purple area
        Box(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .background(Color(0xFF9e7cfe))
        )
    }
}

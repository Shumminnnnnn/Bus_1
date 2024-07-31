package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        modifier = Modifier
            .padding(8.dp)
    ) {
        Button(
            onClick = onButtonClick1,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe))
        ) {
            Text(text = "最新消息")
        }

        Button(
            onClick = onButtonClick2,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe))
        ) {
            Text(text = "路線查詢")
        }

        Button(
            onClick = onButtonClick3,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe))
        ) {
            Text(text = "附近站牌")
        }

        Button(
            onClick = onButtonClick7,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe))
        ) {
            Text(text = "路線規劃")
        }
        Button(
            onClick = onButtonClickSetting,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e7cfe))
        ) {
            Text(text = "設定")
        }
    }
}
package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tdxResult = remember { mutableStateOf("Loading TDX data...") }

                    // Launch Coroutines
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

                    ScrollableContent(
                        onButtonClick1 = {
                            val intent = Intent(this@MainActivity, NewsActivity::class.java)
                            startActivity(intent)
                        },
                        onButtonClick2 = {
                            val intent = Intent(this@MainActivity, RouteFilter::class.java)
                            startActivity(intent)
                        },
                        onButtonClick3 = {
                            val intent = Intent(this@MainActivity, StopActivity::class.java)
                            startActivity(intent)
                        },

//                        onButtonClick5 = {
//                            val intent = Intent(this@MainActivity, PlanActivity::class.java)
//                            startActivity(intent)
//                        },

                        onButtonClick7 = {
                            val intent = Intent(this@MainActivity, PlanFilter::class.java)
                            startActivity(intent)
                        },
                        onButtonClick8 = {
                            val intent = Intent(this@MainActivity, TimeActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollableContent(
    onButtonClick1: () -> Unit,
    onButtonClick2: () -> Unit,
    onButtonClick3: () -> Unit,
//    onButtonClick5: () -> Unit,
    onButtonClick7: () -> Unit,
    onButtonClick8: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Button(
            onClick = onButtonClick1,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "最新消息")
        }

        Button(
            onClick = onButtonClick2,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "路線查詢")
        }

        Button(
            onClick = onButtonClick3,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "附近站牌")
        }

//        Button(
//            onClick = onButtonClick5,
//            modifier = Modifier.padding(top = 16.dp)
//        ) {
//            Text(text = "路線規劃")
//        }

        Button(
            onClick = onButtonClick7,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "路線規劃篩選")
        }

        Button(
            onClick = onButtonClick8,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "時間選擇器")
        }
    }
}

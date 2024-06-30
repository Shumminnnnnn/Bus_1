package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StopActivity : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val routeResult = remember { mutableStateOf("Loading route schedule data...") }

                    // Launch Coroutines
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val routeResultJson = ArroundStop.main()
                            withContext(Dispatchers.Main) {
                                routeResult.value = routeResultJson
                            }
                        } catch (e: Exception) {
                            Log.e("StopActivity", "Error fetching route data: ${e.message}", e)
                            withContext(Dispatchers.Main) {
                                routeResult.value = "Error fetching route data: ${e.message}"
                            }
                        }
                    }

                    ScrollableContent7(routeResult.value)
                }
            }
        }
    }
}

@Composable
fun ScrollableContent7(routeResult: String) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (routeResult == "Loading route schedule data...") {
            Text(text = routeResult, modifier = Modifier.padding(16.dp))
        } else {
            // Parse the result and display each item in a bordered box with centered text
            routeResult.split("\n\n").forEach { routeItem ->
                if (routeItem.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .border(1.dp, Color.Gray)
                            .padding(8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                        ) {
                            routeItem.split("\n").forEach { line ->
                                Text(text = line)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

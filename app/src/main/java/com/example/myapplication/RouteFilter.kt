package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RouteFilter : ComponentActivity() {
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
                    var inputText by remember { mutableStateOf("") }

                    // Launch Coroutines
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val routeResultJson = Route_filter.main()
                            withContext(Dispatchers.Main) {
                                routeResult.value = routeResultJson
                            }
                        } catch (e: Exception) {
                            Log.e("RouteFilter", "Error fetching route data: ${e.message}", e)
                            withContext(Dispatchers.Main) {
                                routeResult.value = "Error fetching route data: ${e.message}"
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            label = { Text("今天搭哪輛公車呢?") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = routeResult.value, modifier = Modifier.padding(16.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomKeyboard(
                            onKeyPress = { key ->
                                when (key) {
                                    "清除" -> inputText = ""
                                    else -> inputText += key
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomKeyboard(onKeyPress: (String) -> Unit) {
    val keys = listOf(
        listOf("L", "1", "2", "3"),
        listOf("GR", "4", "5", "6"),
        listOf("BR", "7", "8", "9"),
        listOf("A", "B", "0", "清除")
    )

    Column {
        keys.forEach { row ->
            Row {
                row.forEach { key ->
                    Button(
                        onClick = {
                            onKeyPress(key)
                        },
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f)
                    ) {
                        Text(text = key)
                    }
                }
            }
        }
    }
}
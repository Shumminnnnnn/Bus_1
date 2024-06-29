package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
                    val routeResult = remember { mutableStateOf("") }
                    var inputText by remember { mutableStateOf("") }

                    fun fetchRouteData(routeNumber: String) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val routeResultJson = Route_filter.main(routeNumber)
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
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Display the input text in a non-clickable TextField
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = {},
                                label = { Text("今天搭哪輛公車呢?") },
                                enabled = false,
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = routeResult.value, modifier = Modifier.padding(16.dp))
                            Spacer(modifier = Modifier.height(16.dp)) // Add extra space to accommodate the keyboard
                        }

                        // Custom keyboard positioned at the bottom
                        CustomKeyboard(
                            onKeyPress = { key ->
                                when (key) {
                                    "清除" -> {
                                        inputText = ""
                                        routeResult.value = ""
                                    }
                                    else -> {
                                        inputText += key
                                        fetchRouteData(inputText)
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .zIndex(1f) // Ensure the keyboard is above other content
                                .background(Color(0xFF6650a4)) // Set the background color for the keyboard (purple)
                                .height(200.dp) // Adjust the height of the keyboard
                        )
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

    Column(modifier = modifier.padding(8.dp).background(Color(0xFF6650a4))) { // Purple background
        keys.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Button(
                        onClick = {
                            onKeyPress(key)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White, // Background color
                            contentColor = Color.Black // Text color
                        ),
                        modifier = Modifier
                            .padding(2.dp) // Adjust the padding to make buttons smaller
                            .weight(1f)
                            .height(40.dp) // Adjust the height of the buttons
                    ) {
                        Text(text = key, fontSize = 14.sp) // Adjust the font size of the button text
                    }
                }
            }
        }
    }
}

package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import java.text.SimpleDateFormat
import java.util.*

class PlanFilter : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tdxResult = remember { mutableStateOf("Loading news data...") }
                    val startLocation = remember { mutableStateOf("") }
                    val endLocation = remember { mutableStateOf("") }
                    val currentTime = remember {
                        mutableStateOf(getCurrentTime())
                    }

                    // Launch Coroutines
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val tdxResultJson = Route_plan.main()
                            withContext(Dispatchers.Main) {
                                tdxResult.value = tdxResultJson
                            }
                        } catch (e: Exception) {
                            Log.e("NewsActivity", "Error fetching TDX data: ${e.message}", e)
                            withContext(Dispatchers.Main) {
                                tdxResult.value = "Error fetching TDX data: ${e.message}"
                            }
                        }
                    }

                    ScrollableContent8(
                        tdxResult = tdxResult.value,
                        startLocation = startLocation.value,
                        onStartLocationChange = { startLocation.value = it },
                        endLocation = endLocation.value,
                        onEndLocationChange = { endLocation.value = it },
                        currentTime = currentTime.value
                    )
                }
            }
        }
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }
}

@Composable
fun ScrollableContent8(
    tdxResult: String,
    startLocation: String,
    onStartLocationChange: (String) -> Unit,
    endLocation: String,
    onEndLocationChange: (String) -> Unit,
    currentTime: String
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TextField(
            value = startLocation,
            onValueChange = onStartLocationChange,
            label = { Text("起點") },
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = endLocation,
            onValueChange = onEndLocationChange,
            label = { Text("終點") },
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "現在時間: $currentTime",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(text = tdxResult)
    }
}

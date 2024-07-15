package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PlanFilter : ComponentActivity() {
    private val startLocationResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleActivityResult(result, "startLocation")
    }

    private val endLocationResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleActivityResult(result, "endLocation")
    }

    private var startLocation by mutableStateOf("")
    private var endLocation by mutableStateOf("")

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
                    val currentTime = remember { mutableStateOf(getCurrentTime()) }

                    // Handle Intent data
                    intent.getStringExtra("startLocation")?.let { startLocation = it }
                    intent.getStringExtra("endLocation")?.let { endLocation = it }

                    // Launch Coroutines
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val tdxResultJson = Route_plan.main()
                            withContext(Dispatchers.Main) {
                                tdxResult.value = tdxResultJson
                            }
                        } catch (e: Exception) {
                            Log.e("PlanFilter", "Error fetching TDX data: ${e.message}", e)
                            withContext(Dispatchers.Main) {
                                tdxResult.value = "Error fetching TDX data: ${e.message}"
                            }
                        }
                    }

                    ScrollableContent8(
                        tdxResult.value,
                        startLocation,
                        { startLocation = it },
                        endLocation,
                        { endLocation = it },
                        currentTime.value,
                        onStartLocationClick = {
                            val intent = Intent(this@PlanFilter, BiginFilter::class.java)
                            startLocationResultLauncher.launch(intent)
                        },
                        onEndLocationClick = {
                            val intent = Intent(this@PlanFilter, EndFilter::class.java)
                            endLocationResultLauncher.launch(intent)
                        }
                    )
                }
            }
        }
    }

    private fun handleActivityResult(result: ActivityResult, key: String) {
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra(key)?.let { value ->
                when (key) {
                    "startLocation" -> startLocation = value
                    "endLocation" -> endLocation = value
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
    currentTime: String,
    onStartLocationClick: () -> Unit,
    onEndLocationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 起點欄位
        OutlinedTextField(
            value = startLocation,
            onValueChange = onStartLocationChange,
            label = { Text("起點") },
            readOnly = true,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clickable { onStartLocationClick() }
                .fillMaxWidth()
        )
        // 終點欄位
        OutlinedTextField(
            value = endLocation,
            onValueChange = onEndLocationChange,
            label = { Text("終點") },
            readOnly = true,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clickable { onEndLocationClick() }
                .fillMaxWidth()
        )
        // 現在時間
        Text(
            text = "現在時間: $currentTime",
            modifier = Modifier.padding(bottom = 8.dp),
            fontSize = 18.sp
        )
        // TDX結果
        Text(text = tdxResult)
    }
}
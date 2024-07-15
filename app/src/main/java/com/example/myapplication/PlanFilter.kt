package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PlanFilter : ComponentActivity() {
    private var startLocation: String by mutableStateOf("")
    private var endLocation: String by mutableStateOf("")
    private var startLat: Double by mutableStateOf(0.0)
    private var startLong: Double by mutableStateOf(0.0)
    private var endLat: Double by mutableStateOf(0.0)
    private var endLong: Double by mutableStateOf(0.0)
    private val currentTime: MutableState<String> = mutableStateOf(getCurrentTime())
    private val tdxResult = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val startLocationResultLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                handleActivityResult(result, "startLocation")
            }

            val endLocationResultLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                handleActivityResult(result, "endLocation")
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val coroutineScope = rememberCoroutineScope()

                    // Fetch TDX data
                    LaunchedEffect(startLat, startLong, endLat, endLong) {
                        coroutineScope.launch {
                            fetchTdxData(startLat, startLong, endLat, endLong, tdxResult)
                        }
                    }

                    PlanFilterContent(
                        startLocation,
                        endLocation,
                        currentTime,
                        tdxResult.value,
                        onNavigateToBiginFilter = {
                            startLocationResultLauncher.launch(Intent(this@PlanFilter, BiginFilter::class.java))
                        }
                    ) {
                        endLocationResultLauncher.launch(
                            Intent(
                                this@PlanFilter,
                                EndFilter::class.java
                            )
                        )
                    }
                }
            }
        }
    }


    private fun handleActivityResult(result: ActivityResult, locationType: String) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                when (locationType) {
                    "startLocation" -> {
                        startLocation = it.getStringExtra("startLocation") ?: ""
                        startLat = it.getDoubleExtra("latitude", 0.0)
                        startLong = it.getDoubleExtra("longitude", 0.0)
                    }
                    "endLocation" -> {
                        endLocation = it.getStringExtra("endLocation") ?: ""
                        endLat = it.getDoubleExtra("latitude", 0.0)
                        endLong = it.getDoubleExtra("longitude", 0.0)
                    }
                }
            }
        }
    }


    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private suspend fun fetchTdxData(
        startLat: Double,
        startLong: Double,
        endLat: Double,
        endLong: Double,
        tdxResult: MutableState<String>
    ) {
        try {
            Route_plan.setLocations(startLat, startLong, endLat, endLong)
            val tdxData = Route_plan.main()
            withContext(Dispatchers.Main) {
                tdxResult.value = tdxData
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                tdxResult.value = "Failed to load data: ${e.message}"
            }
        }
    }
}

@Composable
fun PlanFilterContent(
    startLocation: String,
    endLocation: String,
    currentTime: MutableState<String>,
    tdxResult: String,
    onNavigateToBiginFilter: () -> Unit,
    onNavigateToEndFilter: () -> Unit
) {
    val startLocationResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                // Handle startLocation result
            }
        }
    }

    val endLocationResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                // Handle endLocation result
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "開始地點: $startLocation",
            modifier = Modifier
                .clickable { onNavigateToBiginFilter() }
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "終點地點: $endLocation",
            modifier = Modifier
                .clickable { onNavigateToEndFilter() }
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "目前時間: $currentTime", modifier = Modifier.padding(8.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "TDX 結果:", modifier = Modifier.padding(8.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = tdxResult, modifier = Modifier.padding(8.dp))
    }
}

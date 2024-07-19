package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
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
    private var currentTime: String by mutableStateOf(fetchCurrentTime())
    private var isTimeSelected: Boolean by mutableStateOf(false)
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

            val timeResultLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    data?.let {
                        currentTime = it.getStringExtra("selectedTime") ?: fetchCurrentTime()
                        isTimeSelected = true // Indicate that time is selected from TimeActivity
                    }
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val coroutineScope = rememberCoroutineScope()

                    PlanFilterContent(
                        startLocation,
                        endLocation,
                        currentTime,
                        tdxResult.value,
                        onNavigateToBiginFilter = {
                            startLocationResultLauncher.launch(Intent(this@PlanFilter, BiginFilter::class.java))
                        },
                        onNavigateToEndFilter = {
                            endLocationResultLauncher.launch(Intent(this@PlanFilter, EndFilter::class.java))
                        },
                        onNavigateToTimeActivity = {
                            timeResultLauncher.launch(Intent(this@PlanFilter, TimeActivity::class.java))
                        },
                        onQueryButtonClick = {
                            coroutineScope.launch {
                                fetchTdxData(startLat, startLong, endLat, endLong, currentTime, tdxResult, isTimeSelected)
                            }
                        }
                    )
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

    private fun fetchCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private suspend fun fetchTdxData(
        startLat: Double,
        startLong: Double,
        endLat: Double,
        endLong: Double,
        currentTime: String,
        tdxResult: MutableState<String>,
        isTimeSelected: Boolean
    ) {
        try {
            val (date, time) = parseCurrentTime(currentTime, isTimeSelected)
            Route_plan.updateFormattedDate(date)
            Route_plan.updateStaticTime(time)
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

    private fun parseCurrentTime(currentTime: String, isTimeSelected: Boolean): Pair<String, String> {
        val parts = currentTime.split(" ")
        val date = parts[0]
        var time = parts[1].replace(":", "%3A") // Encode time for URL

        if (!isTimeSelected) {
            // Add 3 minutes to the current time
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.time = sdf.parse(currentTime)!!
            calendar.add(Calendar.MINUTE, 3)
            time = sdf.format(calendar.time).split(" ")[1].replace(":", "%3A")
        }

        return Pair(date, time)
    }
}

@Composable
fun PlanFilterContent(
    startLocation: String,
    endLocation: String,
    currentTime: String,
    tdxResult: String,
    onNavigateToBiginFilter: () -> Unit,
    onNavigateToEndFilter: () -> Unit,
    onNavigateToTimeActivity: () -> Unit,
    onQueryButtonClick: () -> Unit // New parameter for the button click
) {
    var showTdxResult by remember { mutableStateOf(false) } // State to control TDX result visibility

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Start location with box and click effect
        Box(
            modifier = Modifier
                .fillMaxWidth() // Make the box fill the available width
                .padding(8.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary) // Border for the box
                .clickable { onNavigateToBiginFilter() } // Click effect
                .padding(16.dp) // Padding inside the box
        ) {
            Text(
                text = "起點: $startLocation",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // End location with box and click effect
        Box(
            modifier = Modifier
                .fillMaxWidth() // Make the box fill the available width
                .padding(8.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary) // Border for the box
                .clickable { onNavigateToEndFilter() } // Click effect
                .padding(16.dp) // Padding inside the box
        ) {
            Text(
                text = "終點: $endLocation",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Current time with click effect
        Text(
            text = currentTime,
            modifier = Modifier
                .padding(8.dp)
                .clickable { onNavigateToTimeActivity() }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Button to fetch and show TDX result
        Button(
            onClick = {
                showTdxResult = true
                onQueryButtonClick() // Trigger fetching TDX data
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = "查詢")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Conditionally show TDX result
        if (showTdxResult) {
            Text(text = tdxResult, modifier = Modifier.padding(8.dp))
        }
    }
}

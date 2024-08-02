package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay


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
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
                                currentTime =
                                    it.getStringExtra("selectedTime") ?: fetchCurrentTime()
                                isTimeSelected = true
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
                                    startLocationResultLauncher.launch(
                                        Intent(
                                            this@PlanFilter,
                                            BiginFilter::class.java
                                        )
                                    )
                                },
                                onNavigateToEndFilter = {
                                    endLocationResultLauncher.launch(
                                        Intent(
                                            this@PlanFilter,
                                            EndFilter::class.java
                                        )
                                    )
                                },
                                onNavigateToTimeActivity = {
                                    timeResultLauncher.launch(
                                        Intent(
                                            this@PlanFilter,
                                            TimeActivity::class.java
                                        )
                                    )
                                },
                                onQueryButtonClick = {
                                    coroutineScope.launch {
                                        fetchTdxData(
                                            startLat,
                                            startLong,
                                            endLat,
                                            endLong,
                                            currentTime,
                                            tdxResult,
                                            isTimeSelected
                                        )
                                    }
                                }
                            )
                            { onBackClick() }
                        }
                    }
                }
            }
        }
    }
    private fun onBackClick() {
        finish()
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
            Route_plan.setLocations(startLat, startLong, endLat, endLong, endLocation) // 传递 endLocation
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
        var time = parts[1].replace(":", "%3A")

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
    onQueryButtonClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showTdxResult by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .background(Color(0xFF9e7cfe))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF9e7cfe))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick, modifier = Modifier.offset(x = (-13).dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "路線規劃",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.disc),
                                    contentDescription = "Disc Icon",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .height(55.dp)
                                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(20.dp))
                                        .background(Color.White)
                                        .clickable { onNavigateToBiginFilter() }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = buildAnnotatedString {
                                                append("起點: ")
                                                withStyle(style = SpanStyle(color = Color.Black)) {
                                                    append(startLocation)
                                                }
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.map_pin),
                                    contentDescription = "Map Pin Icon",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .height(55.dp)
                                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(20.dp))
                                        .background(Color.White)
                                        .clickable { onNavigateToEndFilter() }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = buildAnnotatedString {
                                                append("終點: ")
                                                withStyle(style = SpanStyle(color = Color.Black)) {
                                                    append(endLocation)
                                                }
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "Search",
                                modifier = Modifier
                                    .size(35.dp)
                                    .clickable {
                                        isLoading = true
                                        showTdxResult = false
                                        onQueryButtonClick()
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF9e7cfe))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onNavigateToTimeActivity() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_access_time_24),
                            contentDescription = "Time Icon",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(25.dp)
                                .offset(x = (-8).dp)
                        )
                        Text(
                            text = "出發時間: $currentTime",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    if (isLoading) {
                        LaunchedEffect(Unit) {
                            delay(2500) // Wait for 2.5 seconds
                            isLoading = false
                            showTdxResult = true
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "載入路線規畫結果中...",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                            )
                        }
                    } else if (showTdxResult) {
                        if (tdxResult.isNotEmpty()) {
                            AnnotatedTdxResult(tdxResult)
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(89.dp)
                                                .offset(y = (-50).dp)
                                                .background(Color(0xFF9e7cfe), shape = CircleShape)
                                        )
                                        Image(
                                            painter = painterResource(id = R.drawable.logo),
                                            contentDescription = "Logo",
                                            modifier = Modifier.size(250.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "查無路線規劃結果!",
                                            style = androidx.compose.ui.text.TextStyle(
                                                fontSize = 18.sp,
                                                color = Color.Black,
                                            ),
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .offset(y = (-140).dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(89.dp)
                                            .offset(y = (-50).dp)
                                            .background(Color(0xFF9e7cfe), shape = CircleShape)
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.logo),
                                        contentDescription = "Logo",
                                        modifier = Modifier.size(250.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "告訴我們你想去哪裡吧!",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 18.sp,
                                            color = Color.Black,
                                        ),
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .offset(y = (-140).dp)
                                    )
                                    Text(
                                        text = "輸入起點和終點，讓我們幫你找到最佳路線!",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 15.sp,
                                            color = Color.Gray,
                                        ),
                                        modifier = Modifier
                                            .offset(y = (-140).dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnotatedTdxResult(tdxResult: String) {
    val annotatedText = buildAnnotatedString {
        val lines = tdxResult.split("\n")
        for (line in lines) {
            when {
                line.contains("[USER_ICON]") -> {
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        appendInlineContent("userIcon", "[USER_ICON]")
                    }
                    append(line.replace("[USER_ICON]", ""))
                }
                line.contains("[baseline_directions_bus_24_ICON]") -> {
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        appendInlineContent("busIcon", "[baseline_directions_bus_24_ICON]")
                    }
                    append(line.replace("[baseline_directions_bus_24_ICON]", ""))
                }
                line.contains("[MINUS_ICON]") -> {
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        appendInlineContent("minusIcon", "[MINUS_ICON]")
                    }
                    append(line.replace("[MINUS_ICON]", ""))
                }
                else -> {
                    if (line.contains(" 分鐘 ") || line.contains(" - ")) {
                        withStyle(style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                            append(line)
                        }
                    } else if (line.startsWith("車資:")) {
                        withStyle(style = SpanStyle(color = Color.Gray)) {
                            append(line)
                        }
                    } else {
                        append(line)
                    }
                }
            }
            append("\n")
        }
    }

    val inlineContent = mapOf(
        "userIcon" to InlineTextContent(
            Placeholder(
                width = 24.sp,
                height = 24.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = "User Icon",
                modifier = Modifier.size(22.dp)
            )
        },
        "busIcon" to InlineTextContent(
            Placeholder(
                width = 24.sp,
                height = 24.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_directions_bus_24),
                contentDescription = "Bus Icon",
                colorFilter = ColorFilter.tint(Color(0xFF9e7cfe)),
                modifier = Modifier.size(24.dp)
            )
        },
        "minusIcon" to InlineTextContent(
            Placeholder(
                width = 24.sp,
                height = 24.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.minus),
                contentDescription = "Minus Icon",
                modifier = Modifier.size(24.dp)
            )
        }
    )

    Text(
        text = annotatedText,
        inlineContent = inlineContent,
        modifier = Modifier.padding(8.dp)
    )
}

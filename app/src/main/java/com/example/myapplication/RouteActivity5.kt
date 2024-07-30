package com.example.myapplication

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

class RouteActivity5 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RouteActivityContent()
                }
            }
        }
    }

    @Composable
    fun RouteActivityContent() {
        val routeInfo = remember { mutableStateOf<RouteData?>(null) }

        LaunchedEffect(Unit) {
            try {
                val fetchedRouteInfo = Route_depdes.main()
                routeInfo.value = fetchedRouteInfo
            } catch (e: Exception) {
                Log.e("RouteActivity5", "Error fetching route data: ${e.message}", e)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top purple area with back button and text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF9e7cfe))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { finish() },
                        modifier = Modifier
                            .background(Color(0xFF9e7cfe))
                            .size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "公車路線簡圖",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            // Scrollable content area
            routeInfo.value?.let { info ->
                ScrollableContent6(
                    routeDepDesInfo = "${info.departureStopNameZh} - ${info.destinationStopNameZh}",
                    routeMapImageUrl = info.routeMapImageUrl
                ) {
                    info.routeMapImageUrl?.let { url ->
                        openExternalLink(url)
                    }
                }
            }

            // Spacer to push the bottom box to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // Bottom purple area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF9e7cfe))
                    .padding(25.dp)
            ) {
                // Content of the bottom box
            }
        }
    }

    private fun openExternalLink(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
}

@Composable
fun ScrollableContent6(
    routeDepDesInfo: String,
    routeMapImageUrl: String?,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(15.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxHeight() // Ensure the column takes full height
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the text
        ) {
            Text(
                text = routeDepDesInfo,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold // Make the text bold
                ),
                modifier = Modifier.padding(10.dp)
            )
        }
        if (!routeMapImageUrl.isNullOrEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center // Center the button
            ) {
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier.padding(top = 16.dp)
                        .offset(y = (-5).dp)
                ) {
                    Text(text = "點我開啟外部連結")
                }
            }
        }
    }
}

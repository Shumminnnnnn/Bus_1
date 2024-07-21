package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = routeDepDesInfo, modifier = Modifier.padding(16.dp))
        Text(
            text = "路線簡圖：",
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )
        if (!routeMapImageUrl.isNullOrEmpty()) {
            Button(
                onClick = onButtonClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "點我開啟外部連結")
            }
        }
    }
}

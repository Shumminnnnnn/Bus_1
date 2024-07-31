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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsActivity : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tdxResult = remember { mutableStateOf("載入最新消息中...") }

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val tdxResultJson = TDXApi.main()
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
                    ScrollableContent3(tdxResult.value) { onBackClick() }
                }
            }
        }
    }
    private fun onBackClick() {
        finish()
    }
}
@Composable
fun ScrollableContent3(tdxResult: String, onBackClick: () -> Unit) {
    val newsItems = tdxResult.split("\n\n")

    Column(
        modifier = Modifier
            .fillMaxSize(1f)
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF9e7cfe))
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "最新消息",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.align(Alignment.Center)
                        .offset(x = (-80).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            newsItems.forEachIndexed { index, newsItem ->
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = newsItem,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(0.98f)
                    )
                }
                if (index < newsItems.size - 2 && newsItem != "載入最新消息中...") {
                    Divider(
                        color = Color(0xFF9e7cfe),
                        thickness = 2.dp,
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .fillMaxWidth(1f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .background(Color(0xFF9e7cfe))
                .fillMaxWidth()
                .padding(vertical = 25.dp)
        )
    }
}

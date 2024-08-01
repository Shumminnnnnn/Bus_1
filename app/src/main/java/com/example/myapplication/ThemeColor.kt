package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

class ThemeColor : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val selectedTheme = remember { mutableStateOf("") }

                    ThemeColorContent(
                        onButtonClickColorBlind = {
                            selectedTheme.value = "Blind"
                            val intent = Intent(this@ThemeColor, Setting::class.java)
                            intent.putExtra("selectedTheme", selectedTheme.value)
                            startActivity(intent)
                        },
                        onButtonClickGeneral = {
                            selectedTheme.value = "Main"
                            val intent = Intent(this@ThemeColor, Setting::class.java)
                            intent.putExtra("selectedTheme", selectedTheme.value)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeColorContent(
    onButtonClickColorBlind: () -> Unit,
    onButtonClickGeneral: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Top purple area with text
        Box(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .background(Color(0xFF9e7cfe)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "主題顏色",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(Color(0xFFE0E0E0))
                    .clickable(onClick = onButtonClickColorBlind)
                    .padding(16.dp)
            ) {
                Text(
                    text = "色覺障礙者友善",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize =20.sp,
                        color = Color.Black
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(Color(0xFFE0E0E0))
                    .clickable(onClick = onButtonClickGeneral)
                    .padding(16.dp)
            ) {
                Text(
                    text = "一般使用者",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                )
            }
        }

        // Bottom purple area
        Box(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .background(Color(0xFF9e7cfe))
        )
    }
}

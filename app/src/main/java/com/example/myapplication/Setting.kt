package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

class Setting : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val selectedTheme = remember { mutableStateOf("") }

                    SettingContent(
                        onButtonClickColorBlind = {
                            selectedTheme.value = "Blind"
                            val returnIntent = Intent(this@Setting, Blind::class.java)
                            startActivity(returnIntent)
                        },
                        onButtonClickGeneral = {
                            selectedTheme.value = "Main"
                            val returnIntent = Intent(this@Setting, MainActivity::class.java)
                            startActivity(returnIntent)
                        },
                        onBackButtonClick = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingContent(
    onButtonClickColorBlind: () -> Unit,
    onButtonClickGeneral: () -> Unit,
    onBackButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
                .background(Color(0xFF9e7cfe)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                IconButton(
                    onClick = onBackButtonClick
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "設定",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(
                text = "主題顏色",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(bottom = 8.dp)
                    .background(Color.White)
                    .clickable(onClick = onButtonClickColorBlind)
                    .padding(16.dp)
            ) {
                Text(
                    text = "色覺障礙者友善",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                )
            }
            Divider(
                color = Color.Gray,
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(Color.White)
                    .clickable(onClick = onButtonClickGeneral)
                    .padding(16.dp)
            ) {
                Text(
                    text = "一般使用者",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .background(Color(0xFF9e7cfe))
        )
    }
}

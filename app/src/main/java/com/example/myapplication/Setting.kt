package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

    private val sharedPref by lazy {
        getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isColorBlindEnabled = remember {
                        mutableStateOf(sharedPref.getBoolean("color_blind_mode", false))
                    }

                    SettingContent(
                        isColorBlindEnabled = isColorBlindEnabled.value,
                        onToggleSwitchChange = { isChecked ->
                            isColorBlindEnabled.value = isChecked
                            sharedPref.edit().putBoolean("color_blind_mode", isChecked).apply()
                        },
                        onBackButtonClick = {
                            if (isColorBlindEnabled.value) {
                                val returnIntent = Intent(this@Setting, Blind::class.java)
                                startActivity(returnIntent)
                            } else {
                                val returnIntent = Intent(this@Setting, MainActivity::class.java)
                                startActivity(returnIntent)
                            }
                            finish() // 結束當前活動
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingContent(
    isColorBlindEnabled: Boolean,
    onToggleSwitchChange: (Boolean) -> Unit,
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
                        modifier = Modifier.size(24.dp)
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
                .padding(10.dp)
                .padding(top = 8.dp)
        ) {
            Text(
                text = "主題顏色",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 9.dp)
                    .padding(bottom = 8.dp)
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "色覺障礙者友善",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isColorBlindEnabled,
                    onCheckedChange = onToggleSwitchChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFbaa2ff),
                        uncheckedThumbColor = Color.Gray
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

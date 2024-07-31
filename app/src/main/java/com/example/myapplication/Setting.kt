package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                    val selectedTheme = intent.getStringExtra("selectedTheme") ?: ""

                    SettingContent(
                        onButtonClickThemeColor = {
                            val intent = Intent(this@Setting, ThemeColor::class.java)
                            startActivity(intent)
                        },
                        onBackButtonClick = {
                            val returnIntent = if (selectedTheme == "Blind") {
                                Intent(this@Setting, Blind::class.java)
                            } else {
                                Intent(this@Setting, MainActivity::class.java)
                            }
                            startActivity(returnIntent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingContent(
    onButtonClickThemeColor: () -> Unit,
    onBackButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Button(
            onClick = onButtonClickThemeColor,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "主題顏色")
        }

        Button(
            onClick = onBackButtonClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "返回")
        }
    }
}

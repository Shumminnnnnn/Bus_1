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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            .padding(8.dp)
    ) {
        Button(
            onClick = onButtonClickColorBlind,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "色覺障礙者友善")
        }

        Button(
            onClick = onButtonClickGeneral,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "一般使用者")
        }
    }
}
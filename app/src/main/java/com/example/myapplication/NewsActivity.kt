package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                    val tdxResult = remember { mutableStateOf("Loading news data...") }

                    // Launch Coroutines
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

                    ScrollableContent3(tdxResult.value)
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Navigate back to MainActivity
        finish()
    }
}
@Composable
fun ScrollableContent3(tdxResult: String) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = tdxResult)
    }
}
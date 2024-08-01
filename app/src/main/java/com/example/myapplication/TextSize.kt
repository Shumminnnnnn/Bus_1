//package com.example.myapplication
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.myapplication.ui.theme.MyApplicationTheme
//
//class TextSize : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MyApplicationTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    var isZoomEnabled by remember { mutableStateOf(false) }
//
//                    TextSizeContent(
//                        onButtonClickLargeText = {
//                            isZoomEnabled = true
//                            navigateToSettings(isZoomEnabled)
//                        },
//                        onButtonClickNormalText = {
//                            isZoomEnabled = false
//                            navigateToSettings(isZoomEnabled)
//                        },
//                        isZoomEnabled = isZoomEnabled
//                    )
//                }
//            }
//        }
//    }
//
//    private fun navigateToSettings(isZoomEnabled: Boolean) {
//        val intent = Intent(this@TextSize, Setting::class.java)
//        intent.putExtra("isZoomEnabled", isZoomEnabled)
//        startActivity(intent)
//        finish()
//    }
//}
//
//@Composable
//fun TextSizeContent(
//    onButtonClickLargeText: () -> Unit,
//    onButtonClickNormalText: () -> Unit,
//    isZoomEnabled: Boolean
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        TextButton(
//            onClick = onButtonClickLargeText,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 8.dp)
//                .background(Color(0xFF9e7cfe))
//                .padding(16.dp)
//        ) {
//            Text(
//                text = "放大",
//                style = androidx.compose.ui.text.TextStyle(
//                    fontSize = 20.sp,
//                    color = Color.White
//                )
//            )
//        }
//        TextButton(
//            onClick = onButtonClickNormalText,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 8.dp)
//                .background(Color(0xFF9e7cfe))
//                .padding(16.dp)
//        ) {
//            Text(
//                text = "正常",
//                style = androidx.compose.ui.text.TextStyle(
//                    fontSize = 20.sp,
//                    color = Color.White
//                )
//            )
//        }
//    }
//}

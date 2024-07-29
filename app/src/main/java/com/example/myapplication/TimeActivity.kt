package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.ClockTestTheme
import java.util.*

class TimeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClockTestTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    DateTimePickerScreen { selectedTime ->
                        val resultIntent = Intent().apply {
                            putExtra("selectedTime", selectedTime)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
fun DateTimePickerScreen(onTimeSelected: (String) -> Unit) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    val context = LocalContext.current
    val purpleColor = Color(0xFF9e7cfe)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(purpleColor)
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                contentDescription = "Back",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .size(24.dp)
                    .clickable {
                        // Navigate back to PlanFilter
                        (context as? ComponentActivity)?.finish()
                    }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "已選擇日期: $date")
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
                        date = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    }, year, month, day).show()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = purpleColor,
                    contentColor = Color.White
                )
            ) {
                Text("選擇日期")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "已選擇時間: $time")
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)
                    TimePickerDialog(context, { _, selectedHour, selectedMinute ->
                        time = "$selectedHour:$selectedMinute"
                        onTimeSelected("$date $time")
                    }, hour, minute, true).show()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = purpleColor,
                    contentColor = Color.White
                )
            ) {
                Text("選擇時間")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(purpleColor)
        )
    }
}

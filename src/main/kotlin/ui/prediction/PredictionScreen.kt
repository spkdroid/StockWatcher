package ui.prediction

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow

@Composable
fun PredictionScreen() {
    var initialAmount by remember { mutableStateOf("") }
    var growthRate by remember { mutableStateOf("") }
    var timePeriod by remember { mutableStateOf("") }
    var projectedAmount by remember { mutableStateOf<Double?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Investment Growth Predictor", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Initial Amount Input
        OutlinedTextField(
            value = initialAmount,
            onValueChange = { initialAmount = it },
            label = { Text("Initial Amount ($)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Growth Rate Input
        OutlinedTextField(
            value = growthRate,
            onValueChange = { growthRate = it },
            label = { Text("Growth Rate (%) per year") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Time Period Input
        OutlinedTextField(
            value = timePeriod,
            onValueChange = { timePeriod = it },
            label = { Text("Time Period (years)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Calculate Button
        Button(
            onClick = {
                val P = initialAmount.toDoubleOrNull() ?: 0.0
                val r = (growthRate.toDoubleOrNull() ?: 0.0) / 100
                val t = timePeriod.toDoubleOrNull() ?: 0.0

                projectedAmount = P * (1 + r).pow(t)
            }
        ) {
            Text("Calculate Projection")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Result
        projectedAmount?.let {
            Text("Projected Amount: $${"%.2f".format(it)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

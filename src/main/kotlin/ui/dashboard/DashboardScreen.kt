package ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.Stock

@Composable
fun DashboardScreen(stocks: List<Stock>, onProjectionSubmit: (String, Double) -> Unit) {
    var selectedStock by remember { mutableStateOf<Stock?>(null) }
    var projection by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Stock Dashboard", style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(16.dp))

        // Stock list
        Text("Current Stocks", style = MaterialTheme.typography.h6)
        stocks.forEach { stock ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stock.symbol)
                Text("Price: $${stock.currentPrice}")
                Button(onClick = { selectedStock = stock }) {
                    Text("Select")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Projection input
        if (selectedStock != null) {
            Text("Selected Stock: ${selectedStock!!.name}", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            BasicTextField(
                value = projection,
                onValueChange = { projection = it },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.padding(8.dp)) {
                        if (projection.isEmpty()) Text("Enter your projection here")
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val projectionValue = projection.toDoubleOrNull()
                if (projectionValue != null) {
                    onProjectionSubmit(selectedStock!!.symbol, projectionValue)
                }
            }) {
                Text("Calculate")
            }
        }
    }
}

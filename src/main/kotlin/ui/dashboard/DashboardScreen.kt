package ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.Stock
import data.network.StockApi
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun DashboardScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<Stock?>(null) }
    val watchlist = remember { mutableStateListOf<Stock>() }
    val scope = rememberCoroutineScope()

    // API request count management
    val dailyRequestLimit = 250
    var requestCount by remember { mutableStateOf(0) }
    var lastRequestDate by remember { mutableStateOf(LocalDate.now()) }

    fun incrementRequestCount() {
        val today = LocalDate.now()
        if (lastRequestDate != today) {
            lastRequestDate = today
            requestCount = 0 // Reset count for a new day
        }
        requestCount++
    }

    // Projection calculation states
    var unitsHeld by remember { mutableStateOf(0) }
    var projectedPrice by remember { mutableStateOf(0.0) }
    var potentialValue by remember { mutableStateOf(0.0) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Title
        Text("Stock Dashboard", style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(16.dp))

        // Disclaimer Section
        Text(
            "Note: This application is on the free tier of the API. Only $dailyRequestLimit requests are allowed per day.",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.error
        )
        Text(
            "Requests Used Today: $requestCount/$dailyRequestLimit",
            style = MaterialTheme.typography.body2
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Section
        Text("Search Stock", style = MaterialTheme.typography.h6)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f).padding(8.dp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.padding(8.dp)) {
                        if (searchQuery.isEmpty()) Text("Enter stock symbol (e.g., AAPL)")
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                scope.launch {
                    if (requestCount < dailyRequestLimit) {
                        searchResult = StockApi.searchStock(searchQuery)
                        incrementRequestCount()
                    } else {
                        println("Daily request limit reached.")
                    }
                }
            }) {
                Text("Search")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Search Result
        searchResult?.let { stock ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${stock.name} (${stock.symbol})")
                Text("Price: $${stock.currentPrice}")
                Button(onClick = {
                    if (!watchlist.contains(stock)) watchlist.add(stock)
                }) {
                    Text("Add to Watchlist")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Watchlist Section
        Text("Watchlist", style = MaterialTheme.typography.h6)
        watchlist.forEach { stock ->
            var price by remember { mutableStateOf(stock.currentPrice) }

            // Start real-time price updates
            LaunchedEffect(stock.symbol) {
                StockApi.getRealTimePrice(stock.symbol).collect { updatedPrice ->
                    price = updatedPrice
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${stock.name} (${stock.symbol})")
                Text("Real-time Price: $${String.format("%.2f", price)}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Projection Calculation Section
        Text("Projection Calculator", style = MaterialTheme.typography.h6)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Units Held:")
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = unitsHeld.toString(),
                onValueChange = { unitsHeld = it.toIntOrNull() ?: 0 },
                modifier = Modifier.width(100.dp).padding(8.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (unitsHeld == 0) Text("0")
                        innerTextField()
                    }
                }
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Projected Price:")
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = projectedPrice.toString(),
                onValueChange = { projectedPrice = it.toDoubleOrNull() ?: 0.0 },
                modifier = Modifier.width(100.dp).padding(8.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (projectedPrice == 0.0) Text("0.0")
                        innerTextField()
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            potentialValue = unitsHeld * projectedPrice
        }) {
            Text("Calculate")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Potential Value: $${String.format("%.2f", potentialValue)}")
    }
}

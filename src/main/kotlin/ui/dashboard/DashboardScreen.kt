package ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.font.FontWeight
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title Section
        Text(
            "Stock Dashboard",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Disclaimer Section
        Card(
            backgroundColor = ComposeColor(0xFFFFE0B2),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Note: This application uses the free tier of the API. Only $dailyRequestLimit requests are allowed per day.",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    "Requests Used Today: $requestCount/$dailyRequestLimit",
                    style = MaterialTheme.typography.body2
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Section
        Text("Search Stock", style = MaterialTheme.typography.h6)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .padding(8.dp),
                decorationBox = { innerTextField ->
                    Box {
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

        // Search Result Section
        searchResult?.let { stock ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${stock.name} (${stock.symbol})", fontWeight = FontWeight.Bold)
                    Text("Price: $${stock.currentPrice}")
                    Button(
                        modifier = Modifier.align(Alignment.End),
                        onClick = {
                            if (!watchlist.contains(stock)) {
                                watchlist.add(stock)
                            }
                        }
                    ) {
                        Text("Add to Watchlist")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Watchlist Section
        Text("Watchlist", style = MaterialTheme.typography.h6)
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        watchlist.forEach { stock ->
            var price by remember { mutableStateOf(stock.currentPrice) }

            // Start real-time price updates
            LaunchedEffect(stock.symbol) {
                StockApi.getRealTimePrice(stock.symbol).collect { updatedPrice ->
                    price = updatedPrice
                }
            }

            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${stock.name} (${stock.symbol})", fontWeight = FontWeight.Bold)
                    Text("Real-time Price: $${String.format("%.2f", price)}")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

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

@Composable
fun DashboardScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<Stock?>(null) }
    val watchlist = remember { mutableStateListOf<Stock>() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Title
        Text("Stock Dashboard", style = MaterialTheme.typography.h4)

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
                    searchResult = StockApi.searchStock(searchQuery)
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
    }
}

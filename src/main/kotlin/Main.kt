import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.Stock
import ui.dashboard.DashboardScreen

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        val mockStocks = listOf(
            Stock("AAPL", "Apple", 150.0, 1.2),
            Stock("GOOGL", "Alphabet", 2800.0, -0.3),
            Stock("AMZN", "Amazon", 3300.0, 0.5)
        )
        DashboardScreen(stocks = mockStocks) { stockSymbol, projection ->
            println("Projection for $stockSymbol: $projection")
        }
    }
}

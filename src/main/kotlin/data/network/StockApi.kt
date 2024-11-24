package data.network

import data.Stock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import java.util.Properties

object StockApi {
    private val config: Properties by lazy {
        Properties().apply {
            val configFile = File("config.properties")
            if (configFile.exists()) {
                load(configFile.inputStream())
            } else {
                throw IllegalStateException("Config file not found! Please create a config.properties file.")
            }
        }
    }

    private val API_KEY: String by lazy {
        config.getProperty("FMP_API_KEY")
            ?: throw IllegalStateException("API key not found in config.properties!")
    }

    private const val BASE_URL = "https://financialmodelingprep.com/api/v3"

    /**
     * Fetch stock details for a given symbol using Financial Modeling Prep.
     */
    suspend fun searchStock(symbol: String): Stock? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/profile/$symbol?apikey=$API_KEY"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(response)

                if (jsonArray.length() > 0) {
                    val stockJson = jsonArray.getJSONObject(0)

                    Stock(
                        symbol = stockJson.getString("symbol"),
                        name = stockJson.getString("companyName"),
                        currentPrice = stockJson.getDouble("price"),
                        changePercentage = stockJson.optDouble("changes", 0.0)
                    )
                } else null
            } else {
                println("Error: Response code ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Fetch real-time price for a given symbol.
     */
    fun getRealTimePrice(symbol: String) = flow {
        while (true) {
            try {
                val stock = searchStock(symbol)
                if (stock != null) {
                    emit(stock.currentPrice) // Emit the updated price
                } else {
                    emit(0.0) // Emit a default value if stock details couldn't be fetched
                }
                delay(500000) // Wait for 5 seconds before fetching the price again
            } catch (e: Exception) {
                e.printStackTrace()
                emit(0.0) // Emit a default value in case of an error
            }
        }
    }
}

data class Stock(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val changePercentage: Double
)

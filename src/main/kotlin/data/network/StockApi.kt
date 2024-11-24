package data.network

import data.Stock
import data.StockTickerMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

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
        config.getProperty("ALPHA_VANTAGE_API_KEY")
            ?: throw IllegalStateException("API key not found in config.properties!")
    }

    private const val BASE_URL = "https://www.alphavantage.co/query"

    /**
     * Fetch stock details for a given symbol using Alpha Vantage.
     */
    suspend fun searchStock(symbol: String): Stock? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL?function=GLOBAL_QUOTE&symbol=$symbol&apikey=$API_KEY"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                println(response)
                val jsonObject = JSONObject(response)
                val globalQuote = jsonObject.getJSONObject("Global Quote")

                val companyName = StockTickerMapper.getCompanyName(symbol)

                Stock(
                    symbol = globalQuote.getString("01. symbol"),
                    name = companyName, // Alpha Vantage doesn’t provide company names in free tier
                    currentPrice = globalQuote.getDouble("05. price"),
                    changePercentage = globalQuote.getString("10. change percent").removeSuffix("%").toDouble()
                )
            } else null
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
                val url = "$BASE_URL?function=GLOBAL_QUOTE&symbol=$symbol&apikey=$API_KEY"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonObject = JSONObject(response)
                    val globalQuote = jsonObject.getJSONObject("Global Quote")

                    val updatedPrice = globalQuote.getDouble("05. price")
                    emit(updatedPrice) // Emit the updated price

                    delay(5000) // Wait for 5 seconds before fetching the price again
                } else {
                    // Error handling can be improved
                    emit(0.0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(0.0) // Emit a default value in case of an error
            }
        }
    }
}
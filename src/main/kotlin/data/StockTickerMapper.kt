package data

import org.json.JSONObject
import java.io.InputStreamReader
import java.nio.charset.Charset

object StockTickerMapper {
    private var stockTickerMap: Map<String, String>? = null

    fun loadStockTickers(context: Any) {
        try {
            val inputStream = context.javaClass.classLoader?.getResourceAsStream("output.json")
                ?: throw Exception("stock_tickers.json file not found")

            val reader = InputStreamReader(inputStream, Charset.defaultCharset())
            val json = JSONObject(reader.readText())
            stockTickerMap = json.keys().asSequence()
                .associateWith { json.getString(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCompanyName(ticker: String): String {
        return stockTickerMap?.get(ticker) ?: "Unknown Company"
    }
}

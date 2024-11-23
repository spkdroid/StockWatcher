package data.network

import data.Stock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import yahoofinance.YahooFinance

object StockApi {

    /**
     * Fetches stock details for a given symbol using Yahoo Finance.
     */
    fun searchStock(symbol: String): Stock? {
        return try {
            val yahooStock = YahooFinance.get(symbol)
            if (yahooStock != null && yahooStock.quote.price != null) {
                Stock(
                    symbol = yahooStock.symbol,
                    name = yahooStock.name ?: "Unknown Company",
                    currentPrice = yahooStock.quote.price.toDouble(),
                    changePercentage = yahooStock.quote.changeInPercent?.toDouble() ?: 0.0
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Provides real-time price updates for a given stock symbol.
     */
    fun getRealTimePrice(symbol: String): Flow<Double> = flow {
        while (true) {
            val updatedPrice = try {
                YahooFinance.get(symbol).quote.price?.toDouble() ?: 0.0
            } catch (e: Exception) {
                e.printStackTrace()
                0.0
            }
            emit(updatedPrice)
            delay(5000L) // Update every 5 seconds
        }
    }
}

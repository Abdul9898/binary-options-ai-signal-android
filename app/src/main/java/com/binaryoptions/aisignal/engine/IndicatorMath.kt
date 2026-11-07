package com.binaryoptions.aisignal.engine

import kotlin.math.pow
import kotlin.math.sqrt

object IndicatorMath {

    fun calculateRSI(prices: List<Double>, period: Int = 14): Double {
        if (prices.size < period + 1) return 50.0

        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val change = prices[i] - prices[i - 1]
            if (change > 0) gains += change else losses -= change
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        for (i in (period + 1) until prices.size) {
            val change = prices[i] - prices[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) -change else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    data class BollingerBands(val upper: Double, val middle: Double, val lower: Double)

    fun calculateBollingerBands(prices: List<Double>, period: Int = 20, multiplier: Double = 2.0): BollingerBands? {
        if (prices.size < period) return null

        val subList = prices.takeLast(period)
        val sma = subList.average()
        val variance = subList.map { (it - sma).pow(2) }.average()
        val stdDev = sqrt(variance)

        return BollingerBands(
            upper = sma + (stdDev * multiplier),
            middle = sma,
            lower = sma - (stdDev * multiplier)
        )
    }

    fun calculateEMA(prices: List<Double>, period: Int): Double? {
        if (prices.size < period) return null
        val k = 2.0 / (period + 1)
        var ema = prices.take(period).average()

        for (i in period until prices.size) {
            ema = (prices[i] * k) + (ema * (1.0 - k))
        }
        return ema
    }
}

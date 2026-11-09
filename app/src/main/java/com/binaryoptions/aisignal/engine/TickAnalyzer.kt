package com.binaryoptions.aisignal.engine

import com.binaryoptions.aisignal.model.SignalDirection
import java.util.ArrayDeque
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class TickRecord(
    val index: Int,
    val timestampMs: Long,
    val price: Double,
    val deltaP: Double,
    val deltaT: Double,
    val velocity: Double, // Price delta per millisecond
    val acceleration: Double // Velocity delta per millisecond
)

data class TickImpulseSignal(
    val direction: SignalDirection,
    val confidence: Double,
    val price: Double,
    val velocityRatio: Double,
    val streak: Int,
    val reason: String,
    val timestampMs: Long,
    val tickIndex: Int
)

class TickAnalyzer(
    private val bufferSize: Int = 60,
    private val velocitySurgeRatio: Double = 2.2,
    private val minStreak: Int = 2
) {
    private val ticks = ArrayDeque<TickRecord>()
    var streak: Int = 0
        private set
    private var lastDirection: Int = 0
    var tickCount: Int = 0
        private set

    @Synchronized
    fun feedTick(price: Double, timestampMs: Long = System.currentTimeMillis()): TickImpulseSignal? {
        tickCount++

        if (ticks.isEmpty()) {
            ticks.add(TickRecord(tickCount, timestampMs, price, 0.0, 1.0, 0.0, 0.0))
            return null
        }

        val prev = ticks.last
        val dt = max((timestampMs - prev.timestampMs).toDouble(), 1.0)
        val dp = price - prev.price

        val velocity = dp / dt
        val dv = velocity - prev.velocity
        val acceleration = dv / dt

        // Directional streak update
        when {
            dp > 0 -> {
                streak = if (lastDirection > 0) streak + 1 else 1
                lastDirection = 1
            }
            dp < 0 -> {
                streak = if (lastDirection < 0) streak - 1 else -1
                lastDirection = -1
            }
            else -> {
                streak = 0
                lastDirection = 0
            }
        }

        val currentRecord = TickRecord(tickCount, timestampMs, price, dp, dt, velocity, acceleration)
        if (ticks.size >= bufferSize) {
            ticks.removeFirst()
        }
        ticks.add(currentRecord)

        if (ticks.size < 15) return null

        return evaluateImpulse(currentRecord)
    }

    private fun evaluateImpulse(current: TickRecord): TickImpulseSignal? {
        val list = ticks.toList()
        if (list.size < 20) return null

        val recentTicks = list.subList(list.size - 20, list.size - 1)
        val avgAbsV = recentTicks.map { abs(it.velocity) }.average()
        if (avgAbsV <= 1e-12) return null

        val currAbsV = abs(current.velocity)
        val surgeRatio = currAbsV / avgAbsV

        // Micro Bollinger Bands over last 20 ticks
        val bbTicks = list.takeLast(20)
        val prices = bbTicks.map { it.price }
        val meanP = prices.average()
        val variance = prices.map { (it - meanP) * (it - meanP) }.average()
        val stdP = if (variance > 0) sqrt(variance) else 0.0

        val upperBand = meanP + (2.0 * stdP)
        val lowerBand = meanP - (2.0 * stdP)

        // Micro-trend context over last 10 ticks
        val last10 = list.takeLast(10)
        val consecutiveDowns = last10.count { it.deltaP < 0 }
        val consecutiveUps = last10.count { it.deltaP > 0 }

        // -------------------------------------------------------------
        // SETUP A: Bullish Impulse (CALL)
        // Anti-Falling-Knife Guard: consecutiveDowns < 7
        // -------------------------------------------------------------
        if (
            streak >= minStreak &&
            surgeRatio >= velocitySurgeRatio &&
            current.velocity > 0 &&
            current.acceleration >= 0 &&
            consecutiveDowns < 7
        ) {
            val conf = min(90.0 + (surgeRatio * 2.0), 98.8)
            val reason = "🚀 TICK IMPULSE UP (+${streak} streak) • Vel: ${String.format("%.1fx", surgeRatio)}"
            return TickImpulseSignal(
                direction = SignalDirection.CALL,
                confidence = Math.round(conf * 10.0) / 10.0,
                price = current.price,
                velocityRatio = surgeRatio,
                streak = streak,
                reason = reason,
                timestampMs = current.timestampMs,
                tickIndex = current.index
            )
        }

        // -------------------------------------------------------------
        // SETUP B: Bearish Impulse (PUT)
        // Guard: consecutiveUps < 7
        // -------------------------------------------------------------
        if (
            streak <= -minStreak &&
            surgeRatio >= velocitySurgeRatio &&
            current.velocity < 0 &&
            current.acceleration <= 0 &&
            consecutiveUps < 7
        ) {
            val conf = min(90.0 + (surgeRatio * 2.0), 98.8)
            val reason = "🔻 TICK BREAK DOWN (${streak} streak) • Vel: ${String.format("%.1fx", surgeRatio)}"
            return TickImpulseSignal(
                direction = SignalDirection.PUT,
                confidence = Math.round(conf * 10.0) / 10.0,
                price = current.price,
                velocityRatio = surgeRatio,
                streak = abs(streak),
                reason = reason,
                timestampMs = current.timestampMs,
                tickIndex = current.index
            )
        }

        // -------------------------------------------------------------
        // SETUP C: Confirmed Micro-Band Rejection
        // -------------------------------------------------------------
        if (current.price <= lowerBand && streak >= 2 && current.velocity > 0 && surgeRatio >= 1.8) {
            val conf = min(89.5 + (surgeRatio * 1.8), 97.5)
            val reason = "⚡ CONFIRMED LOWER BAND BOUNCE (+${streak}t) • Vel: ${String.format("%.1fx", surgeRatio)}"
            return TickImpulseSignal(
                direction = SignalDirection.CALL,
                confidence = Math.round(conf * 10.0) / 10.0,
                price = current.price,
                velocityRatio = surgeRatio,
                streak = streak,
                reason = reason,
                timestampMs = current.timestampMs,
                tickIndex = current.index
            )
        }

        if (current.price >= upperBand && streak <= -2 && current.velocity < 0 && surgeRatio >= 1.8) {
            val conf = min(89.5 + (surgeRatio * 1.8), 97.5)
            val reason = "⚡ CONFIRMED UPPER BAND REJECTION (${streak}t) • Vel: ${String.format("%.1fx", surgeRatio)}"
            return TickImpulseSignal(
                direction = SignalDirection.PUT,
                confidence = Math.round(conf * 10.0) / 10.0,
                price = current.price,
                velocityRatio = surgeRatio,
                streak = abs(streak),
                reason = reason,
                timestampMs = current.timestampMs,
                tickIndex = current.index
            )
        }

        return null
    }
}

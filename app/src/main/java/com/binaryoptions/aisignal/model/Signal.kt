package com.binaryoptions.aisignal.model

enum class SignalDirection {
    CALL,
    PUT
}

enum class SignalStatus {
    ACTIVE,
    WIN,
    LOSS
}

data class Signal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val pair: String,
    val direction: SignalDirection,
    val strikePrice: Double,
    var currentPrice: Double,
    val durationTicks: Int = 5,
    val expirySeconds: Int = 5,
    val confidence: Double,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis(),
    val entryTickIndex: Int = 0,
    val targetTickIndex: Int = entryTickIndex + durationTicks,
    var ticksElapsed: Int = 0,
    var remainingSeconds: Int = expirySeconds,
    var status: SignalStatus = SignalStatus.ACTIVE,
    val expiryTime: Long = createdAt + (expirySeconds * 1000L)
) {
    fun isExpired(): Boolean = status != SignalStatus.ACTIVE || ticksElapsed >= durationTicks

    fun updateWithTick(price: Double, currentTickIndex: Int) {
        currentPrice = price
        ticksElapsed++
        remainingSeconds = maxOf(durationTicks - ticksElapsed, 0)

        if ((ticksElapsed >= durationTicks || currentTickIndex >= targetTickIndex) && status == SignalStatus.ACTIVE) {
            status = if (direction == SignalDirection.CALL) {
                if (currentPrice > strikePrice) SignalStatus.WIN else SignalStatus.LOSS
            } else {
                if (currentPrice < strikePrice) SignalStatus.WIN else SignalStatus.LOSS
            }
        }
    }

    fun updatePrice(price: Double) {
        currentPrice = price
        val remaining = ((expiryTime - System.currentTimeMillis()) / 1000).toInt()
        remainingSeconds = if (remaining > 0) remaining else 0
        if ((remainingSeconds == 0 || isExpired()) && status == SignalStatus.ACTIVE) {
            status = if (direction == SignalDirection.CALL) {
                if (currentPrice > strikePrice) SignalStatus.WIN else SignalStatus.LOSS
            } else {
                if (currentPrice < strikePrice) SignalStatus.WIN else SignalStatus.LOSS
            }
        }
    }
}

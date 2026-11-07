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
    val expirySeconds: Int, // e.g. 60 for 1M, 300 for 5M
    val confidence: Double, // e.g. 93.5%
    val reason: String,
    val createdAt: Long = System.currentTimeMillis(),
    var remainingSeconds: Int = expirySeconds,
    var status: SignalStatus = SignalStatus.ACTIVE,
    val expiryTime: Long = createdAt + (expirySeconds * 1000L)
) {
    fun isExpired(): Boolean = System.currentTimeMillis() >= expiryTime

    fun updatePrice(price: Double) {
        currentPrice = price
        val remaining = ((expiryTime - System.currentTimeMillis()) / 1000).toInt()
        remainingSeconds = if (remaining > 0) remaining else 0
        if (remainingSeconds == 0 && status == SignalStatus.ACTIVE) {
            // Settle signal
            status = if (direction == SignalDirection.CALL) {
                if (currentPrice >= strikePrice) SignalStatus.WIN else SignalStatus.LOSS
            } else {
                if (currentPrice <= strikePrice) SignalStatus.WIN else SignalStatus.LOSS
            }
        }
    }
}

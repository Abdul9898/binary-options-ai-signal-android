package com.binaryoptions.aisignal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.binaryoptions.aisignal.R
import com.binaryoptions.aisignal.databinding.ItemSignalBinding
import com.binaryoptions.aisignal.model.Signal
import com.binaryoptions.aisignal.model.SignalDirection
import com.binaryoptions.aisignal.model.SignalStatus
import java.util.*

class SignalAdapter(
    private var items: List<Signal>
) : RecyclerView.Adapter<SignalAdapter.SignalViewHolder>() {

    fun updateList(newItems: List<Signal>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignalViewHolder {
        val binding = ItemSignalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SignalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SignalViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class SignalViewHolder(private val binding: ItemSignalBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(signal: Signal) {
            val context = itemView.context
            val isTwoDecimal = signal.pair.contains("USDT") || signal.pair.contains("Volatility")
            val priceFormat = if (isTwoDecimal) "%.2f" else "%.5f"

            binding.tvPair.text = signal.pair
            binding.tvExpiry.text = "${signal.durationTicks} TICKS"
            binding.tvStrikePrice.text = String.format(Locale.US, priceFormat, signal.strikePrice)
            binding.tvCurrentPrice.text = String.format(Locale.US, priceFormat, signal.currentPrice)
            binding.tvConfidence.text = "${signal.confidence}%"
            binding.tvReason.text = signal.reason

            // Direction button
            if (signal.direction == SignalDirection.CALL) {
                binding.tvDirection.text = "CALL 🔼"
                binding.tvDirection.setBackgroundResource(R.drawable.bg_btn_call)
                val isWinning = signal.currentPrice >= signal.strikePrice
                binding.tvCurrentPrice.setTextColor(ContextCompat.getColor(context, if (isWinning) R.color.signal_call else R.color.signal_put))
            } else {
                binding.tvDirection.text = "PUT 🔽"
                binding.tvDirection.setBackgroundResource(R.drawable.bg_btn_put)
                val isWinning = signal.currentPrice <= signal.strikePrice
                binding.tvCurrentPrice.setTextColor(ContextCompat.getColor(context, if (isWinning) R.color.signal_call else R.color.signal_put))
            }

            // Status Badge & Countdown
            when (signal.status) {
                SignalStatus.ACTIVE -> {
                    binding.tvStatus.text = "ACTIVE ⏳"
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_expiry)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_gold))
                    binding.tvCountdown.text = "${signal.ticksElapsed}/${signal.durationTicks} TICKS"
                    binding.tvCountdown.setTextColor(ContextCompat.getColor(context, R.color.accent_gold))
                }
                SignalStatus.WIN -> {
                    binding.tvStatus.text = "WIN ✅"
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_win)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.black))
                    binding.tvCountdown.text = "SETTLED"
                    binding.tvCountdown.setTextColor(ContextCompat.getColor(context, R.color.signal_call))
                }
                SignalStatus.LOSS -> {
                    binding.tvStatus.text = "LOSS ❌"
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_loss)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                    binding.tvCountdown.text = "SETTLED"
                    binding.tvCountdown.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
                }
            }
        }
    }
}

package com.isekco.vestia.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.isekco.vestia.R
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class AssetAdapter(
    private var items: List<AssetUiModel> = emptyList()
) : RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    fun submitList(newItems: List<AssetUiModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asset_summary, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val assetCard: MaterialCardView = itemView.findViewById(R.id.assetCard)
        private val assetColorIndicator: View = itemView.findViewById(R.id.assetColorIndicator)
        private val assetNameText: TextView = itemView.findViewById(R.id.assetNameText)
        private val assetQuantityText: TextView = itemView.findViewById(R.id.assetQuantityText)
        private val assetRateText: TextView = itemView.findViewById(R.id.assetRateText)
        private val assetValueTlText: TextView = itemView.findViewById(R.id.assetValueTlText)

        fun bind(item: AssetUiModel) {

            val unit = when (item.assetLabel) {
                "XAU" -> "g"
                "USD" -> "$"
                "EUR" -> "€"
                "GBP" -> "£"
                else -> ""
            }

            assetNameText.text = item.assetLabel
            assetQuantityText.text = formatQuantity(item.quantity, unit = unit)

            assetRateText.text = "₺ ${item.tryRate} / ${unit}"

            assetValueTlText.text = formatTry(item.totalValueTry)

            assetColorIndicator.setBackgroundResource(dotDrawableRes(item.assetLabel))

            assetCard.strokeWidth = dpToPx(itemView, 1)
            assetCard.strokeColor = itemView.context.getColor(R.color.vestia_border)
            assetCard.cardElevation = dpToPx(itemView, 2).toFloat()
        }

        private fun dotDrawableRes(assetKey: String): Int {
            return when (assetKey.uppercase(Locale.US)) {
                "USD" -> R.drawable.bg_legend_dot_usd
                "XAU" -> R.drawable.bg_legend_dot_xau
                "GBP" -> R.drawable.bg_legend_dot_gbp
                "EUR" -> R.drawable.bg_legend_dot_eur
                else -> R.drawable.bg_legend_dot_usd
            }
        }

        private fun formatQuantity(value: BigDecimal, unit: String): String {
            val df = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))

            //return df.format(value) + " " + unit
            return "${df.format(value)} $unit"
        }

        private fun formatRate(value: BigDecimal): String {
            val df = DecimalFormat("#,##0.0000", DecimalFormatSymbols(Locale.US))
            return df.format(value)
        }

        private fun formatTry(value: BigDecimal): String {
            val symbols = DecimalFormatSymbols(Locale("tr", "TR")).apply {
                decimalSeparator = ','
                groupingSeparator = '.'
            }
            val df = DecimalFormat("₺#,##0.00", symbols)
            return df.format(value)
        }

        private fun dpToPx(view: View, dp: Int): Int {
            val density = view.resources.displayMetrics.density
            return (dp * density).toInt()
        }
    }
}
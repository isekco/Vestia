package com.isekco.vestia.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.isekco.vestia.R

class AssetAdapter : RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    private val items = mutableListOf<AssetUiModel>()

    fun submitList(newItems: List<AssetUiModel>) {
        items.clear()
        items.addAll(newItems)
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

        private val assetNameText: TextView = itemView.findViewById(R.id.assetNameText)
        private val assetQuantityValueText: TextView = itemView.findViewById(R.id.assetQuantityText)
        private val assetRateValueText: TextView = itemView.findViewById(R.id.assetRateText)
        private val assetTotalValueText: TextView = itemView.findViewById(R.id.assetValueTlText)

        fun bind(item: AssetUiModel) {
            assetNameText.text = item.assetName
            assetQuantityValueText.text = item.quantityText
            assetRateValueText.text = item.rateText
            assetTotalValueText.text = item.totalValueText
        }
    }
}
package com.isekco.vestia.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.isekco.vestia.R

class AssetAdapter(
    private val onAssetTypeClicked: ((AssetTypeUiModel) -> Unit)? = null
) : RecyclerView.Adapter<AssetAdapter.AssetTypeViewHolder>() {

    private val items = mutableListOf<AssetTypeUiModel>()

    fun submitList(newItems: List<AssetTypeUiModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asset_type, parent, false)
        return AssetTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetTypeViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class AssetTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val headerLayout: View = itemView.findViewById(R.id.headerLayout)
        private val assetTypeIcon: ImageView = itemView.findViewById(R.id.assetTypeIcon)
        private val assetTypeText: TextView = itemView.findViewById(R.id.assetTypeText)
        private val assetTotalValueText: TextView = itemView.findViewById(R.id.assetTotalValueText)
        private val expandIcon: ImageView = itemView.findViewById(R.id.expandIcon)
        private val instrumentRecyclerView: RecyclerView =
            itemView.findViewById(R.id.instrumentRecyclerView)

        private val instrumentAdapter = AssetInstrumentAdapter()

        init {
            instrumentRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            instrumentRecyclerView.adapter = instrumentAdapter
            instrumentRecyclerView.setHasFixedSize(false)
            instrumentRecyclerView.isNestedScrollingEnabled = false
        }

        fun bind(item: AssetTypeUiModel) {
            assetTypeText.text = item.assetTypeName
            assetTotalValueText.text = item.totalValueText

            bindAssetTypeIcon(item)
            bindExpandedState(item.isExpanded)

            instrumentAdapter.submitList(item.instruments)

            headerLayout.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                val currentItem = items[currentPosition]
                val updatedItem = currentItem.copy(isExpanded = !currentItem.isExpanded)

                items[currentPosition] = updatedItem
                notifyItemChanged(currentPosition)

                onAssetTypeClicked?.invoke(updatedItem)
            }
        }

        private fun bindExpandedState(itemIsExpanded: Boolean) {
            instrumentRecyclerView.visibility = if (itemIsExpanded) View.VISIBLE else View.GONE
            expandIcon.rotation = if (itemIsExpanded) 180f else 0f
        }

        private fun bindAssetTypeIcon(item: AssetTypeUiModel) {
            val iconRes = when (item.assetTypeName.uppercase()) {
                "CASH" -> R.drawable.ic_cash
                "XAU" -> R.drawable.ic_gold
                else -> R.drawable.ic_cash
            }
            assetTypeIcon.setImageResource(iconRes)
        }
    }

    private class AssetInstrumentAdapter :
        RecyclerView.Adapter<AssetInstrumentAdapter.AssetInstrumentViewHolder>() {

        private val items = mutableListOf<AssetInstrumentUiModel>()

        fun submitList(newItems: List<AssetInstrumentUiModel>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetInstrumentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_asset_instrument, parent, false)
            return AssetInstrumentViewHolder(view)
        }

        override fun onBindViewHolder(holder: AssetInstrumentViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        class AssetInstrumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val instrumentNameText: TextView =
                itemView.findViewById(R.id.instrumentNameText)
            private val instrumentQuantityText: TextView =
                itemView.findViewById(R.id.instrumentQuantityText)
            private val instrumentRateText: TextView =
                itemView.findViewById(R.id.instrumentRateText)
            private val instrumentTotalValueText: TextView =
                itemView.findViewById(R.id.instrumentTotalValueText)

            fun bind(item: AssetInstrumentUiModel) {
                instrumentNameText.text = item.instrumentName
                instrumentQuantityText.text = item.quantityText
                instrumentRateText.text = item.rateText
                instrumentTotalValueText.text = item.totalValueText
            }
        }
    }
}
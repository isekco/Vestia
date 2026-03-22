package com.isekco.vestia.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.isekco.vestia.R

data class OwnerListItemUiModel(
    val id: String,
    val name: String
)

class OwnerListAdapter(
    private val onEditClick: (OwnerListItemUiModel) -> Unit,
    private val onDeleteClick: (OwnerListItemUiModel) -> Unit
) : RecyclerView.Adapter<OwnerListAdapter.OwnerViewHolder>() {

    private val items = mutableListOf<OwnerListItemUiModel>()

    fun submitList(newItems: List<OwnerListItemUiModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_owner_row, parent, false)
        return OwnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: OwnerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class OwnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ownerNameText: TextView = itemView.findViewById(R.id.ownerNameText)
        private val editOwnerButton: ImageButton = itemView.findViewById(R.id.editOwnerButton)
        private val deleteOwnerButton: ImageButton = itemView.findViewById(R.id.deleteOwnerButton)

        fun bind(item: OwnerListItemUiModel) {
            ownerNameText.text = item.name

            editOwnerButton.setOnClickListener {
                onEditClick(item)
            }

            deleteOwnerButton.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }
}
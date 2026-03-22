package com.isekco.vestia.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.isekco.vestia.R

class ManagementBottomSheet : BottomSheetDialogFragment() {

    private var ownersExpanded = false

    private lateinit var ownersHeaderLayout: LinearLayout
    private lateinit var ownersContentLayout: LinearLayout
    private lateinit var ownersExpandIcon: TextView
    private lateinit var ownersRecyclerView: RecyclerView
    private lateinit var addOwnerText: TextView
    private lateinit var accountsMenuText: TextView
    private lateinit var transactionsMenuText: TextView
    private lateinit var ownerListAdapter: OwnerListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.management_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupOwnerRecyclerView()
        setupClicks()
        loadMockOwners()
    }

    private fun bindViews(view: View) {
        ownersHeaderLayout = view.findViewById(R.id.ownersHeaderLayout)
        ownersContentLayout = view.findViewById(R.id.ownersContentLayout)
        ownersExpandIcon = view.findViewById(R.id.ownersExpandIcon)
        ownersRecyclerView = view.findViewById(R.id.ownersRecyclerView)
        addOwnerText = view.findViewById(R.id.addOwnerText)
        accountsMenuText = view.findViewById(R.id.accountsMenuText)
        transactionsMenuText = view.findViewById(R.id.transactionsMenuText)
    }

    private fun setupOwnerRecyclerView() {
        ownerListAdapter = OwnerListAdapter(
            onEditClick = { owner ->
                Toast.makeText(
                    requireContext(),
                    "Edit ${owner.name}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDeleteClick = { owner ->
                Toast.makeText(
                    requireContext(),
                    "Delete ${owner.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        ownersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ownersRecyclerView.adapter = ownerListAdapter
    }

    private fun setupClicks() {
        ownersHeaderLayout.setOnClickListener {
            ownersExpanded = !ownersExpanded
            ownersContentLayout.visibility = if (ownersExpanded) View.VISIBLE else View.GONE
            ownersExpandIcon.text = if (ownersExpanded) "▲" else "▼"
        }

        addOwnerText.setOnClickListener {
            Toast.makeText(requireContext(), "Add Owner clicked", Toast.LENGTH_SHORT).show()
        }

        accountsMenuText.setOnClickListener {
            Toast.makeText(requireContext(), "Accounts clicked", Toast.LENGTH_SHORT).show()
        }

        transactionsMenuText.setOnClickListener {
            Toast.makeText(requireContext(), "Transactions clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMockOwners() {
        val owners = listOf(
            OwnerListItemUiModel(id = "o1", name = "Owner1"),
            OwnerListItemUiModel(id = "o2", name = "Owner2"),
            OwnerListItemUiModel(id = "o3", name = "Owner3")
        ).sortedBy { it.name.lowercase() }

        ownerListAdapter.submitList(owners)
    }
}
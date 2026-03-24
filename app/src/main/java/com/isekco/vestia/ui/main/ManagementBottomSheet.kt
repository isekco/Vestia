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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.isekco.vestia.R
import com.isekco.vestia.domain.usecase.LoadOwnersUseCase
import com.isekco.vestia.domain.usecase.AddOwnerUseCase
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.isekco.vestia.VestiaApp

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

    private val loadOwnersUseCase: LoadOwnersUseCase by lazy {
        (requireActivity().application as VestiaApp).container.loadOwnersUseCase
    }

    private val addOwnerUseCase: AddOwnerUseCase by lazy {
        (requireActivity().application as VestiaApp).container.addOwnerUseCase
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.management_bottom_sheet, container, false)
    }

    override fun onStart() {
        super.onStart()

        val bottomSheetDialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = bottomSheetDialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.peekHeight = resources.displayMetrics.heightPixels
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupOwnerRecyclerView()
        setupClicks()
        loadOwners()
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
            showAddOwnerDialog()
        }

        accountsMenuText.setOnClickListener {
            Toast.makeText(requireContext(), "Accounts clicked", Toast.LENGTH_SHORT).show()
        }

        transactionsMenuText.setOnClickListener {
            Toast.makeText(requireContext(), "Transactions clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadOwners() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val owners = loadOwnersUseCase(forceRefresh = false)
                    .map { owner ->
                        OwnerListItemUiModel(
                            id = owner.id,
                            name = owner.name
                        )
                    }

                ownerListAdapter.submitList(owners)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.message ?: "Owner listesi yüklenemedi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showAddOwnerDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Owner name"
            setSingleLine(true)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Owner")
            .setView(input)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    val positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    positiveButton.setOnClickListener {
                        val ownerName = input.text.toString()
                        addOwner(ownerName, dialog)
                    }
                }
                dialog.show()
            }
    }

    private fun addOwner(ownerName: String, dialog: androidx.appcompat.app.AlertDialog) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val newOwner = addOwnerUseCase(ownerName)

                Toast.makeText(
                    requireContext(),
                    "Added ${newOwner.name}",
                    Toast.LENGTH_SHORT
                ).show()

                loadOwners()
                dialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.message ?: "Owner could not be added",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
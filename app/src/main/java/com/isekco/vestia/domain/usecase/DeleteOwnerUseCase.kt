package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.model.Owner
import com.isekco.vestia.domain.repository.LedgerRepository

class DeleteOwnerUseCase(
    private val ledgerRepository: LedgerRepository
) {
    suspend operator fun invoke(ownerId: String): Owner {
        val currentLedger = ledgerRepository.getLedger(forceRefresh = false)

        val ownerToDelete = currentLedger.owners.find { it.id == ownerId }
            ?: throw IllegalArgumentException("Owner not found")

        val hasAccounts = currentLedger.accounts.any { account ->
            account.ownerId == ownerId
        }
        require(!hasAccounts) { "This owner has linked accounts. Delete or move them first." }

        val hasTransactions = currentLedger.transactions.any { transaction ->
            transaction.ownerId == ownerId
        }
        require(!hasTransactions) { "This owner has linked transactions. Delete them first." }

        val updatedOwners = currentLedger.owners.filterNot { owner ->
            owner.id == ownerId
        }

        val updatedLedger = currentLedger.copy(owners = updatedOwners)

        ledgerRepository.saveLedger(updatedLedger)

        return ownerToDelete
    }
}
package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.model.Owner
import com.isekco.vestia.domain.repository.LedgerRepository

class EditOwnerUseCase(
    private val ledgerRepository: LedgerRepository
) {
    suspend operator fun invoke(ownerId: String, newName: String): Owner {
        val trimmedName = newName.trim()

        require(trimmedName.isNotBlank()) { "Owner name must not be blank" }

        val currentLedger = ledgerRepository.getLedger(forceRefresh = false)

        val ownerToEdit = currentLedger.owners.find { it.id == ownerId }
            ?: throw IllegalArgumentException("Owner not found")

        val duplicateExists = currentLedger.owners.any { owner ->
            owner.id != ownerId && owner.name.equals(trimmedName, ignoreCase = true)
        }
        require(!duplicateExists) { "Owner already exists" }

        val updatedOwner = ownerToEdit.copy(name = trimmedName)

        val updatedOwners = currentLedger.owners.map { owner ->
            if (owner.id == ownerId) updatedOwner else owner
        }

        val updatedLedger = currentLedger.copy(owners = updatedOwners)

        ledgerRepository.saveLedger(updatedLedger)

        return updatedOwner
    }
}
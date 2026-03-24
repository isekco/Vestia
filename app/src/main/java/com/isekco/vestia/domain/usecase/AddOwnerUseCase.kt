package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.model.Ledger
import com.isekco.vestia.domain.model.Owner
import com.isekco.vestia.domain.repository.LedgerRepository

class AddOwnerUseCase(
    private val ledgerRepository: LedgerRepository
) {
    suspend operator fun invoke(name: String): Owner {
        val trimmedName = name.trim()

        require(trimmedName.isNotBlank()) { "Owner name must not be blank" }

        val currentLedger = ledgerRepository.getLedger(forceRefresh = false)

        val alreadyExists = currentLedger.owners.any {
            it.name.equals(trimmedName, ignoreCase = true)
        }
        require(!alreadyExists) { "Owner already exists" }

        val newOwner = Owner(
            id = nextOwnerId(currentLedger),
            name = trimmedName
        )

        val updatedLedger = currentLedger.copy(
            owners = currentLedger.owners + newOwner
        )

        ledgerRepository.saveLedger(updatedLedger)

        return newOwner
    }

    private fun nextOwnerId(ledger: Ledger): String {
        val maxIdNumber = ledger.owners
            .mapNotNull { owner ->
                owner.id.removePrefix("o").toIntOrNull()
            }
            .maxOrNull() ?: 0

        return "o${maxIdNumber + 1}"
    }
}
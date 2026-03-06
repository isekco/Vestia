package com.isekco.vestia.data.datasource

import android.content.Context
import java.io.File

class LedgerDataSource(
    private val context: Context,
    private val fileName: String = "ledger.json"
) {
    private val localFile: File
        get() = File(context.filesDir, fileName)

    private fun ensureSeeded() {
        if (localFile.exists()) return
        context.assets.open(fileName).use { input ->
            localFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    fun readLedgerJson(): String {
        ensureSeeded()
        return localFile.readText()
    }

    fun writeLedgerJson(json: String) {
        ensureSeeded()

        val tmp = File(context.filesDir, "$fileName.tmp")
        tmp.writeText(json)

        if (localFile.exists()) localFile.delete()

        if (!tmp.renameTo(localFile)) {
            // renameTo bazen fail olabilir → fallback
            localFile.writeText(tmp.readText())
            tmp.delete()
        }
    }
}
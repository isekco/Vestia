package com.isekco.vestia.data.datasource

import android.content.Context
import java.io.File

class LedgerDataSource(
    private val context: Context,
    private val assetFileName: String = "ledger.json"
) {

    private val localFile: File
        get() = File(context.filesDir, assetFileName)

    /**
     * Local dosya yoksa assets içinden seed eder.
     */
    private fun ensureSeeded() {
        if (localFile.exists()) return

        context.assets.open(assetFileName).use { input ->
            localFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    fun readLedgerJson(): String {
        ensureSeeded()
        return localFile.readText()
    }

    /**
     * Ledger JSON'u local dosyaya yazar (overwrite).
     * Basit atomic yaklaşım: temp dosyaya yazıp rename.
     */
    fun writeLedgerJson(json: String) {
        ensureSeeded()

        val tmp = File(context.filesDir, "$assetFileName.tmp")
        tmp.writeText(json)

        if (localFile.exists()) {
            localFile.delete()
        }

        if (!tmp.renameTo(localFile)) {
            // renameTo bazı cihazlarda başarısız olabilir; fallback
            localFile.writeText(tmp.readText())
            tmp.delete()
        }
    }
}
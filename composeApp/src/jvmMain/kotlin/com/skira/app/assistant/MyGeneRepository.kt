package com.skira.app.assistant

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


@Serializable
private data class MyGeneQueryResponse(
    val hits: List<MyGeneHit> = emptyList()
)

@Serializable
private data class MyGeneHit(
    val symbol: String = "",
    val name: String = "",
    @SerialName("ZFIN") val zfin: String? = null,
    val entrezgene: String? = null,
    val summary: String? = null,
    // MyGene can return this field as either an object or an array depending on gene.
    @SerialName("genomic_pos") val genomicPos: JsonElement? = null,
    val agr: MyGeneAgr? = null,
    val go: MyGeneGO? = null
)

@Serializable
private data class MyGeneGO(
    @SerialName("BP") val biologicalProcess: List<MyGeneGoTerm> = emptyList(),
    @SerialName("CC") val cellularComponent: List<MyGeneGoTerm> = emptyList()
)

@Serializable
private data class MyGeneGoTerm(
    val term: String = ""
)


@Serializable
private data class MyGeneAgr(
    val orthologs: List<MyGeneOrtholog> = emptyList()
)

@Serializable
private data class MyGeneOrtholog(
    val taxid: Int = 0,
    val symbol: String = ""
)


object MyGeneRepository {

    private val client = OkHttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Fetches gene metadata from MyGene.info for the given zebrafish gene symbol (species=7955).
     * Returns [MyGeneInfoData] on success or a failure with a descriptive cause.
     */
    suspend fun fetchGeneInfo(geneSymbol: String): Result<MyGeneInfoData> = withContext(Dispatchers.IO) {
        val url = "https://mygene.info/v3/query" +
                "?q=symbol:${geneSymbol.trim()}" +
                "&species=7955" +
                "&fields=name,symbol,ZFIN,entrezgene,summary,genomic_pos,agr,go"

        val request = Request.Builder()
            .url(url)
            .header("accept", "application/json")
            .build()

        runCatching {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            val body = response.body?.string()
                ?: throw IOException("Empty response body")

            val queryResponse = json.decodeFromString<MyGeneQueryResponse>(body)
            val hit = queryResponse.hits.firstOrNull()
                ?: throw NoSuchElementException("No entry found for gene '$geneSymbol' in zebrafish")

            val zfinUrl = if (hit.zfin != null)
                "https://zfin.org/${hit.zfin}"
            else
                "https://zfin.org"

            val ncbiUrl = if (hit.entrezgene != null)
                "https://www.ncbi.nlm.nih.gov/gene/${hit.entrezgene}"
            else
                "https://www.ncbi.nlm.nih.gov"

            // Killifish (N. furzeri, TaxID: 105023) NCBI search link
            val killifishSearchUrl = "https://www.ncbi.nlm.nih.gov/gene/?term=${hit.symbol}+AND+txid105023[orgn]"

            // Use summary, fallback to first BP (Biological Process) term
            val description = if (!hit.summary.isNullOrBlank()) {
                hit.summary
            } else {
                hit.go?.biologicalProcess?.firstOrNull()?.term?.let { "Involved in $it." }
                    ?: ""
            }

            // Location: first CC (Cellular Component) term
            val location = hit.go?.cellularComponent?.firstOrNull()?.term

            val humanOrtholog = hit.agr?.orthologs
                ?.firstOrNull { it.taxid == 9606 }
                ?.symbol

            MyGeneInfoData(
                symbol = hit.symbol,
                fullName = hit.name,
                zfinUrl = zfinUrl,
                ncbiUrl = ncbiUrl,
                killifishSearchUrl = killifishSearchUrl,
                description = description,
                location = location,
                humanOrtholog = humanOrtholog
            )
        }
    }
}


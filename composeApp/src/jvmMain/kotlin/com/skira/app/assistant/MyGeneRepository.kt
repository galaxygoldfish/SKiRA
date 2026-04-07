package com.skira.app.assistant

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder

object MyGeneRepository {

    private val client = OkHttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Fetches gene metadata from Alliance Genome (ZFIN-backed) for the given gene symbol.
     * Returns [MyGeneInfoData] on success or a failure with a descriptive cause.
     */
    suspend fun fetchGeneInfo(geneSymbol: String): Result<MyGeneInfoData> = withContext(Dispatchers.IO) {
        runCatching {
            val query = geneSymbol.trim()
            require(query.isNotBlank()) { "Gene symbol cannot be blank" }

            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://www.alliancegenome.org/api/search?q=$encodedQuery&category=gene"

            val searchRequest = Request.Builder()
                .url(searchUrl)
                .header("accept", "application/json")
                .build()

            val searchBody = client.newCall(searchRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Alliance search HTTP ${response.code}")
                }
                response.body?.string() ?: throw IOException("Alliance search returned empty response body")
            }

            val searchRoot = json.parseToJsonElement(searchBody).jsonObject
            val results = searchRoot.arrayField("results")

            val zfinCandidates = results
                .mapNotNull { it as? JsonObject }
                .filter { (it.stringField("id") ?: "").startsWith("ZFIN:") }

            val selectedHit = zfinCandidates.firstOrNull { hit ->
                val symbol = hit.stringField("symbol") ?: return@firstOrNull false
                symbol.equals(query, ignoreCase = true)
            } ?: zfinCandidates.firstOrNull()
            ?: throw NoSuchElementException("No ZFIN entry found for '$query' in Alliance Genome search")

            val allianceId = selectedHit.stringField("id")
                ?: throw NoSuchElementException("Alliance search hit is missing an id for '$query'")

            val detailsUrl = "https://www.alliancegenome.org/api/gene/${URLEncoder.encode(allianceId, "UTF-8")}"
            val detailsRequest = Request.Builder()
                .url(detailsUrl)
                .header("accept", "application/json")
                .build()

            val detailsBody = client.newCall(detailsRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Alliance gene details HTTP ${response.code}")
                }
                response.body?.string() ?: throw IOException("Alliance gene details returned empty response body")
            }

            val details = json.parseToJsonElement(detailsBody).jsonObject
            val symbol = details.stringField("symbol")
                ?: selectedHit.stringField("symbol")
                ?: query
            val fullName = details.stringField("name") ?: symbol
            val description = details.stringField("automatedGeneSynopsis")
                ?: details.stringField("geneSynopsis")
                ?: details.stringField("automatedGeneDescription")
                ?: ""

            val zfinUrl = details.stringField("modCrossRefCompleteUrl")
                ?: "https://zfin.org/${allianceId.removePrefix("ZFIN:")}"

            val ncbiQuery = URLEncoder.encode("$symbol AND txid7955[orgn]", "UTF-8")
            val ncbiUrl = "https://www.ncbi.nlm.nih.gov/gene/?term=$ncbiQuery"

            val killifishQuery = URLEncoder.encode("$symbol AND txid105023[orgn]", "UTF-8")
            val killifishSearchUrl = "https://www.ncbi.nlm.nih.gov/gene/?term=$killifishQuery"

            val location = details.arrayField("genomeLocations").firstOrNull()
                ?.let { it as? JsonObject }
                ?.let { genome ->
                    val chromosome = genome.stringField("chromosome")
                    val start = genome.numberField("start")
                    val end = genome.numberField("end")
                    val assembly = genome.stringField("assembly")
                    val coordinates = if (chromosome != null && start != null && end != null) {
                        "chr$chromosome:$start-$end"
                    } else {
                        null
                    }
                    when {
                        coordinates != null && assembly != null -> "$coordinates ($assembly)"
                        coordinates != null -> coordinates
                        else -> assembly
                    }
                }

            MyGeneInfoData(
                symbol = symbol,
                fullName = fullName,
                zfinUrl = zfinUrl,
                ncbiUrl = ncbiUrl,
                killifishSearchUrl = killifishSearchUrl,
                description = description,
                location = location,
                humanOrtholog = null
            )
        }
    }

    private fun JsonObject.stringField(key: String): String? {
        val primitive = this[key] as? JsonPrimitive ?: return null
        return primitive.contentOrNull?.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.numberField(key: String): String? {
        val primitive = this[key] as? JsonPrimitive ?: return null
        return primitive.contentOrNull?.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.arrayField(key: String): JsonArray {
        return (this[key] as? JsonArray) ?: JsonArray(emptyList())
    }

}

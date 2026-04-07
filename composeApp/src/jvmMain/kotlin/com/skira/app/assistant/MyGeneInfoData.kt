package com.skira.app.assistant
import kotlinx.serialization.Serializable

/**
 * Represents a response from the MyGeneInfo API
 */
@Serializable
data class MyGeneInfoData(
    val symbol: String,
    val fullName: String,
    val zfinUrl: String,
    val ncbiUrl: String,
    val killifishSearchUrl: String,
    val description: String,
    val location: String?,
    val humanOrtholog: String?
)
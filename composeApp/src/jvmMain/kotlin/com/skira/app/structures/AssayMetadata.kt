package com.skira.app.structures

import kotlinx.serialization.Serializable

/**
 * Representing the metadata of our assays coming from the R objects to be used in the user-facing views
 */
@Serializable
data class AssayMetadata(
    /* List of genes by name */
    val genes: List<String> = emptyList(),
    /* List of timepoints, units are hours post-fertilization (postfix of hpf included in each entry) */
    val timepoints: List<String> = emptyList()
)
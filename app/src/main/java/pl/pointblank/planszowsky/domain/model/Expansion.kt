package pl.pointblank.planszowsky.domain.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Expansion(
    val id: String,
    val title: String,
    val isOwned: Boolean = false
)

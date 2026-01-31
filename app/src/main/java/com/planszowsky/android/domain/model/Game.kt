package com.planszowsky.android.domain.model

data class Game(
    val id: String,
    val title: String,
    val thumbnailUrl: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val yearPublished: String? = null,
    val minPlayers: String? = null,
    val maxPlayers: String? = null,
    val playingTime: String? = null,
    val isOwned: Boolean = false,
    val isWishlisted: Boolean = false,
    val isBorrowed: Boolean = false,
    val borrowedTo: String? = null
)

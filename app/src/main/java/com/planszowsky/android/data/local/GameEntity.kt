package com.planszowsky.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.planszowsky.android.domain.model.Game

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val imageUrl: String?,
    val description: String?,
    val yearPublished: String?,
    val minPlayers: String?,
    val maxPlayers: String?,
    val playingTime: String?,
    val isOwned: Boolean,
    val isWishlisted: Boolean,
    val isBorrowed: Boolean = false,
    val borrowedTo: String? = null,
    val categories: List<String> = emptyList()
)

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
}

fun GameEntity.toDomainModel(): Game {
    return Game(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        imageUrl = imageUrl,
        description = description,
        yearPublished = yearPublished,
        minPlayers = minPlayers,
        maxPlayers = maxPlayers,
        playingTime = playingTime,
        isOwned = isOwned,
        isWishlisted = isWishlisted,
        isBorrowed = isBorrowed,
        borrowedTo = borrowedTo,
        categories = categories
    )
}

fun Game.toEntity(): GameEntity {
    return GameEntity(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        imageUrl = imageUrl,
        description = description,
        yearPublished = yearPublished,
        minPlayers = minPlayers,
        maxPlayers = maxPlayers,
        playingTime = playingTime,
        isOwned = isOwned,
        isWishlisted = isWishlisted,
        isBorrowed = isBorrowed,
        borrowedTo = borrowedTo,
        categories = categories
    )
}

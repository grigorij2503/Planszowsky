package pl.pointblank.planszowsky.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import pl.pointblank.planszowsky.domain.model.Game

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
    val isFavorite: Boolean = false,
    val isBorrowed: Boolean = false,
    val borrowedTo: String? = null,
    val isBorrowedFrom: Boolean = false,
    val borrowedFrom: String? = null,
    val notes: String? = null,
    val categories: List<String> = emptyList(),
    val ownerId: String? = null
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
        isFavorite = isFavorite,
        isBorrowed = isBorrowed,
        borrowedTo = borrowedTo,
        isBorrowedFrom = isBorrowedFrom,
        borrowedFrom = borrowedFrom,
        notes = notes,
        categories = categories,
        ownerId = ownerId
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
        isFavorite = isFavorite,
        isBorrowed = isBorrowed,
        borrowedTo = borrowedTo,
        isBorrowedFrom = isBorrowedFrom,
        borrowedFrom = borrowedFrom,
        notes = notes,
        categories = categories,
        ownerId = ownerId
    )
}

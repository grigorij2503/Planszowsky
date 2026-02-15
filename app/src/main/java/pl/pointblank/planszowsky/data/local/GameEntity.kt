package pl.pointblank.planszowsky.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.domain.model.Expansion

@Entity(
    tableName = "games",
    primaryKeys = ["id", "collectionId"]
)
data class GameEntity(
    val id: String,
    val collectionId: String = "main",
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
    val expansions: List<Expansion> = emptyList(),
    val websiteUrl: String? = null
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

    private val mapper = jacksonObjectMapper()

    @TypeConverter
    fun fromExpansionList(value: List<Expansion>?): String {
        return mapper.writeValueAsString(value ?: emptyList<Expansion>())
    }

    @TypeConverter
    fun toExpansionList(value: String?): List<Expansion> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            mapper.readValue(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

fun GameEntity.toDomainModel(): Game {
    return Game(
        id = id,
        collectionId = collectionId,
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
        expansions = expansions,
        websiteUrl = websiteUrl
    )
}

fun Game.toEntity(): GameEntity {
    return GameEntity(
        id = id,
        collectionId = collectionId,
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
        expansions = expansions,
        websiteUrl = websiteUrl
    )
}

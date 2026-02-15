package pl.pointblank.planszowsky.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey val id: String, // "main" dla lokalnej, UUID dla zewnętrznych
    val name: String,
    val sourceUrl: String? = null,
    val isReadOnly: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

package pl.pointblank.planszowsky.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class PlayerScore(
    val id: String,
    val name: String,
    val score: Int = 0,
    val color: Int // ARGB Int
)

@Entity(tableName = "active_sessions")
data class SessionEntity(
    @PrimaryKey val gameId: String,
    val gameTitle: String,
    val startTime: Long,
    val players: List<PlayerScore>,
    val notes: String = "",
    val isActive: Boolean = true
)

class SessionConverters {
    private val mapper = jacksonObjectMapper()

    @TypeConverter
    fun fromPlayerScoreList(value: List<PlayerScore>?): String {
        return mapper.writeValueAsString(value ?: emptyList<PlayerScore>())
    }

    @TypeConverter
    fun toPlayerScoreList(value: String?): List<PlayerScore> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            mapper.readValue(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

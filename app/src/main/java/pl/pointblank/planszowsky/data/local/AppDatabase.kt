package pl.pointblank.planszowsky.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [GameEntity::class, CollectionEntity::class], version = 9, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create collections table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS collections (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        sourceUrl TEXT,
                        isReadOnly INTEGER NOT NULL DEFAULT 0,
                        lastUpdated INTEGER NOT NULL
                    )
                """.trimIndent())

                // 2. Insert default main collection
                db.execSQL("""
                    INSERT INTO collections (id, name, isReadOnly, lastUpdated)
                    VALUES ('main', 'Moja Kolekcja', 0, ${System.currentTimeMillis()})
                """.trimIndent())

                // 3. Create new games table with composite primary key
                // Match Room's expected schema exactly (no DEFAULT values in schema if not in @ColumnInfo)
                db.execSQL("""
                    CREATE TABLE games_new (
                        id TEXT NOT NULL,
                        collectionId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        thumbnailUrl TEXT,
                        imageUrl TEXT,
                        description TEXT,
                        yearPublished TEXT,
                        minPlayers TEXT,
                        maxPlayers TEXT,
                        playingTime TEXT,
                        isOwned INTEGER NOT NULL,
                        isWishlisted INTEGER NOT NULL,
                        isFavorite INTEGER NOT NULL,
                        isBorrowed INTEGER NOT NULL,
                        borrowedTo TEXT,
                        isBorrowedFrom INTEGER NOT NULL,
                        borrowedFrom TEXT,
                        notes TEXT,
                        categories TEXT NOT NULL,
                        websiteUrl TEXT,
                        PRIMARY KEY(id, collectionId)
                    )
                """.trimIndent())

                // 4. Copy data from old games table to new games table
                db.execSQL("""
                    INSERT INTO games_new (
                        id, collectionId, title, thumbnailUrl, imageUrl, description,
                        yearPublished, minPlayers, maxPlayers, playingTime,
                        isOwned, isWishlisted, isFavorite, isBorrowed, borrowedTo,
                        isBorrowedFrom, borrowedFrom, notes, categories, websiteUrl
                    )
                    SELECT 
                        id, 'main', title, thumbnailUrl, imageUrl, description,
                        yearPublished, minPlayers, maxPlayers, playingTime,
                        isOwned, isWishlisted, isFavorite, isBorrowed, borrowedTo,
                        isBorrowedFrom, borrowedFrom, notes, categories, websiteUrl
                    FROM games
                """.trimIndent())

                // 5. Drop old table and rename new one
                db.execSQL("DROP TABLE games")
                db.execSQL("ALTER TABLE games_new RENAME TO games")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN notes TEXT")
                db.execSQL("ALTER TABLE games ADD COLUMN isBorrowedFrom INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE games ADD COLUMN borrowedFrom TEXT")
            }
        }
        
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN ownerId TEXT")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN websiteUrl TEXT")
            }
        }
    }
}

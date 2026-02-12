package pl.pointblank.planszowsky.domain.model

data class CollectionStats(
    val totalOwned: Int = 0,
    val wishlistCount: Int = 0,
    val favoriteCount: Int = 0,
    val lentCount: Int = 0,
    val topCategory: String? = null
)

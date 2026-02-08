package pl.pointblank.planszowsky.util


/**
 * Calculates the Levenshtein distance between two strings.
 * Lower distance means strings are more similar.
 */
fun String.levenshteinDistance(other: String): Int {
    if (this == other) return 0
    if (this.isEmpty()) return other.length
    if (other.isEmpty()) return this.length

    val thisLen = this.length
    val otherLen = other.length
    var cost = IntArray(thisLen + 1) { it }
    var newCost = IntArray(thisLen + 1)

    for (i in 1..otherLen) {
        newCost[0] = i
        for (j in 1..thisLen) {
            val match = if (this[j - 1] == other[i - 1]) 0 else 1
            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1
            newCost[j] = minOf(costInsert, costDelete, costReplace)
        }
        val swap = cost
        cost = newCost
        newCost = swap
    }
    return cost[thisLen]
}

/**
 * Calculates similarity score (0.0 to 1.0).
 * 1.0 means exact match.
 */
fun String.similarity(other: String): Double {
    val maxLength = maxOf(this.length, other.length)
    if (maxLength == 0) return 1.0
    val distance = this.levenshteinDistance(other)
    return 1.0 - (distance.toDouble() / maxLength)
}

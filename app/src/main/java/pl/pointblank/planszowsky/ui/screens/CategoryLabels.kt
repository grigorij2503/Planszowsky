package pl.pointblank.planszowsky.ui.screens

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.pointblank.planszowsky.R
import java.util.Locale

@Composable
internal fun categoryDisplayName(category: String): String {
    val resource = categoryLabelResource(category) ?: return category
    return stringResource(resource)
}

@Composable
internal fun categoryDisplayNames(categories: List<String>): String {
    val labels = mutableListOf<String>()
    for (category in categories) {
        labels += categoryDisplayName(category)
    }
    return labels.joinToString(", ")
}

@StringRes
private fun categoryLabelResource(category: String): Int? {
    return when (category.trim().lowercase(Locale.ROOT)) {
        "card game" -> R.string.category_card_game
        "children's game" -> R.string.category_childrens_game
        "fantasy" -> R.string.category_fantasy
        "economic" -> R.string.category_economic
        "war game" -> R.string.category_war_game
        "adventure" -> R.string.category_adventure
        "dice" -> R.string.category_dice
        "party game" -> R.string.category_party_game
        "abstract strategy" -> R.string.category_abstract_strategy
        "horror" -> R.string.category_horror
        "science fiction" -> R.string.category_science_fiction
        "medieval" -> R.string.category_medieval
        "civilization" -> R.string.category_civilization
        "exploration" -> R.string.category_exploration
        "ancient" -> R.string.category_ancient
        else -> null
    }
}

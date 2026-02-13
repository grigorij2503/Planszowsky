package pl.pointblank.planszowsky.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageManager {
    fun applyLocale(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
            if (languageCode == "system") {
                localeManager.applicationLocales = LocaleList.getEmptyLocaleList()
            } else {
                localeManager.applicationLocales = LocaleList.forLanguageTags(languageCode)
            }
        } else {
            val appLocale = if (languageCode == "system") {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(languageCode)
            }
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun getCurrentLocale(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
            val locales = localeManager.applicationLocales
            if (locales.isEmpty) "system" else locales.toLanguageTags()
        } else {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (locales.isEmpty) "system" else locales.toLanguageTags()
        }
    }
}
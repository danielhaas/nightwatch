package com.nightwatch.model

enum class Language(val code: String, val displayName: String, val speechCode: String) {
    DE("de", "Deutsch", "de-DE"),
    EN("en", "English", "en-US"),
    FR("fr", "Français", "fr-FR"),
    IT("it", "Italiano", "it-IT");

    companion object {
        fun fromCode(code: String): Language =
            entries.firstOrNull { it.code == code } ?: DE
    }
}

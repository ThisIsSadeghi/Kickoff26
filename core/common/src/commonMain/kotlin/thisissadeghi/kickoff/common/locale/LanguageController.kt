package thisissadeghi.kickoff.common.locale

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the currently selected language and persists changes.
 *
 * Exposed as a Koin singleton (see `initKoin.kt`). The app root collects
 * [language] and feeds it to `LocalAppLocale`; the picker calls [setLanguage].
 *
 * `null` ⇒ follow the system locale.
 */
class LanguageController(
    private val store: LanguagePreferenceStore,
) {
    private val _language = MutableStateFlow(store.getLanguageTag())

    /** Selected BCP-47 language tag, or `null` to follow the system locale. */
    val language: StateFlow<String?> = _language.asStateFlow()

    fun setLanguage(tag: String?) {
        store.setLanguageTag(tag)
        _language.value = tag
    }
}

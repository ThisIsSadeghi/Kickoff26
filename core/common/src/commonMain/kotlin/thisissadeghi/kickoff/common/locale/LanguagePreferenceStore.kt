package thisissadeghi.kickoff.common.locale

/**
 * Persists the user's selected language tag across launches.
 *
 * `null` means "follow the system locale" (no override stored).
 * Platform implementations are bound in `commonPlatformModule` (see `CommonModules`).
 */
interface LanguagePreferenceStore {
    fun getLanguageTag(): String?

    fun setLanguageTag(tag: String?)
}

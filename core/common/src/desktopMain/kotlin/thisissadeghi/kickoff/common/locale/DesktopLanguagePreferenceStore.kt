package thisissadeghi.kickoff.common.locale

import java.util.prefs.Preferences

internal class DesktopLanguagePreferenceStore : LanguagePreferenceStore {
    private val prefs = Preferences.userRoot().node("common/locale")

    override fun getLanguageTag(): String? = prefs.get(KEY, null)

    override fun setLanguageTag(tag: String?) {
        if (tag == null) prefs.remove(KEY) else prefs.put(KEY, tag)
    }

    private companion object {
        const val KEY = "language_tag"
    }
}

package thisissadeghi.kickoff.common.locale

import platform.Foundation.NSUserDefaults

internal class IosLanguagePreferenceStore : LanguagePreferenceStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getLanguageTag(): String? = defaults.stringForKey(KEY)

    override fun setLanguageTag(tag: String?) {
        if (tag == null) defaults.removeObjectForKey(KEY) else defaults.setObject(tag, KEY)
    }

    private companion object {
        const val KEY = "app_language_tag"
    }
}

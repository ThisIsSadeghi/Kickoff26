package thisissadeghi.kickoff.common.locale

import android.content.Context
import androidx.core.content.edit

internal class AndroidLanguagePreferenceStore(
    context: Context,
) : LanguagePreferenceStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getLanguageTag(): String? = prefs.getString(KEY, null)

    override fun setLanguageTag(tag: String?) {
        prefs.edit {
            if (tag == null) remove(KEY) else putString(KEY, tag)
        }
    }

    private companion object {
        const val PREFS_NAME = "app_locale"
        const val KEY = "language_tag"
    }
}

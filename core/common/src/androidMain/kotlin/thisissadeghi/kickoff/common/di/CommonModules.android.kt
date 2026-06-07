package thisissadeghi.kickoff.common.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import thisissadeghi.kickoff.common.locale.AndroidLanguagePreferenceStore
import thisissadeghi.kickoff.common.locale.LanguagePreferenceStore
import thisissadeghi.kickoff.common.util.AndroidLinkHandler
import thisissadeghi.kickoff.common.util.LinkHandler

/**
 * Created by Ali Sadeghi
 * on 07,May,2025
 */
internal actual val commonPlatformModule: Module =
    module {
        singleOf(::AndroidLinkHandler).bind<LinkHandler>()
        single<LanguagePreferenceStore> { AndroidLanguagePreferenceStore(androidContext()) }
    }

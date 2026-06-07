package thisissadeghi.kickoff.common.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import thisissadeghi.kickoff.common.locale.IosLanguagePreferenceStore
import thisissadeghi.kickoff.common.locale.LanguagePreferenceStore
import thisissadeghi.kickoff.common.util.IOSLinkHandler
import thisissadeghi.kickoff.common.util.LinkHandler

/**
 * Created by Ali Sadeghi
 * on 07,May,2025
 */

internal actual val commonPlatformModule: Module =
    module {
        singleOf(::IOSLinkHandler).bind<LinkHandler>()
        singleOf(::IosLanguagePreferenceStore).bind<LanguagePreferenceStore>()
    }

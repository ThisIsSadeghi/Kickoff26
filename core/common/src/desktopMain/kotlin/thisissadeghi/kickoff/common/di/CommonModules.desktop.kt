package thisissadeghi.kickoff.common.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import thisissadeghi.kickoff.common.locale.DesktopLanguagePreferenceStore
import thisissadeghi.kickoff.common.locale.LanguagePreferenceStore
import thisissadeghi.kickoff.common.util.DesktopLinkHandler
import thisissadeghi.kickoff.common.util.LinkHandler

internal actual val commonPlatformModule: Module =
    module {
        singleOf(::DesktopLinkHandler).bind<LinkHandler>()
        singleOf(::DesktopLanguagePreferenceStore).bind<LanguagePreferenceStore>()
    }

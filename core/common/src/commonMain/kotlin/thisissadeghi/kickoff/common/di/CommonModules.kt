package thisissadeghi.kickoff.common.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import thisissadeghi.kickoff.common.locale.LanguageController

/**
 * Created by Ali Sadeghi
 * on 07,May,2025
 */

internal expect val commonPlatformModule: Module

internal val localeModule: Module =
    module {
        singleOf(::LanguageController)
    }

val commonModule: Module =
    module {
        includes(commonPlatformModule, localeModule)
    }

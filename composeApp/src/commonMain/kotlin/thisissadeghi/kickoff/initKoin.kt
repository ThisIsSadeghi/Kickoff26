package thisissadeghi.kickoff

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import thisissadeghi.kickoff.common.di.commonModule
import thisissadeghi.kickoff.data.config.BuildOptionProvider
import thisissadeghi.kickoff.data.di.dataModule
import thisissadeghi.kickoff.home.di.homeModule

private val appModule =
    module {
        singleOf(::BuildOptionProviderImpl).bind<BuildOptionProvider>()
    }

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(
            appModule,
            commonModule,
            dataModule,
            homeModule,
        )
    }

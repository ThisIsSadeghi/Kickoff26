package thisissadeghi.kickoff.home.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import thisissadeghi.kickoff.home.data.datasource.HomeRemoteDataSource
import thisissadeghi.kickoff.home.data.datasource.HomeRemoteDataSourceImpl
import thisissadeghi.kickoff.home.data.repository.HomeRepository
import thisissadeghi.kickoff.home.data.repository.HomeRepositoryImpl
import thisissadeghi.kickoff.home.presentation.HomeViewModel

val homeModule: Module =
    module {
        singleOf(::HomeRemoteDataSourceImpl).bind<HomeRemoteDataSource>()
        singleOf(::HomeRepositoryImpl).bind<HomeRepository>()
        viewModelOf(::HomeViewModel)
    }

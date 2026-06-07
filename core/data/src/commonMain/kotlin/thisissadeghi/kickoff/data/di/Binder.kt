package thisissadeghi.kickoff.data.di

import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import thisissadeghi.kickoff.data.datasource.local.AuthenticationLocalDataSource
import thisissadeghi.kickoff.data.datasource.local.AuthenticationLocalDataSourceImpl
import thisissadeghi.kickoff.data.repository.user.UserRepository
import thisissadeghi.kickoff.data.repository.user.UserRepositoryImpl

/**
 * Data layer dependency injection bindings
 */
internal val binder =
    module {
        // JSON serializer
        single {
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            }
        }

        // Authentication Local DataSource (used by login feature)
        singleOf(::AuthenticationLocalDataSourceImpl).bind<AuthenticationLocalDataSource>()

        // User
        singleOf(::UserRepositoryImpl).bind<UserRepository>()
    }

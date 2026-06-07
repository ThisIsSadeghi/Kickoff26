package thisissadeghi.kickoff.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue
import org.koin.dsl.bind
import org.koin.dsl.module
import thisissadeghi.kickoff.data.local.pref.PreferencesManager
import thisissadeghi.kickoff.data.token.TokenManager
import thisissadeghi.kickoff.data.token.TokenManagerImpl
import thisissadeghi.kickoff.data.voucher.VoucherManager
import thisissadeghi.kickoff.data.voucher.VoucherManagerImpl

internal const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"

object DataStorePathStringQualifier : Qualifier {
    override val value: QualifierValue
        get() = DataStorePathStringQualifier::class.simpleName.toString()
}

internal expect val platformLocalDataSourceModule: Module

internal val localDataSourceModule =
    module {
        single { PreferencesManager(get()) }

        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath(
                produceFile = { get<String>(qualifier = DataStorePathStringQualifier).toPath() },
            )
        }

        // Token Manager
        singleOf(::TokenManagerImpl).bind<TokenManager>()

        // Voucher Manager
        singleOf(::VoucherManagerImpl).bind<VoucherManager>()
    }

/*
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "X-db",
        ).build()
    }*/

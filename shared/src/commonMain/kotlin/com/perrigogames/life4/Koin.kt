package com.perrigogames.life4

import com.perrigogames.life4.db.GoalDatabaseHelper
import com.perrigogames.life4.db.ResultDatabaseHelper
import com.perrigogames.life4.db.SongDatabaseHelper
import com.perrigogames.life4.db.TrialDatabaseHelper
import com.perrigogames.life4.ktor.GithubDataAPI
import com.perrigogames.life4.ktor.GithubDataImpl
import com.perrigogames.life4.ktor.Life4API
import com.perrigogames.life4.ktor.Life4APIImpl
import com.perrigogames.life4.model.*
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(platformModule, coreModule)
}

val coreModule = module {
    single { GoalDatabaseHelper(get()) }
    single { ResultDatabaseHelper(get()) }
    single { SongDatabaseHelper(get()) }
    single { TrialDatabaseHelper(get()) }
    single<GithubDataAPI> { GithubDataImpl() }
    single<Life4API> { Life4APIImpl() }
    single { Json { classDiscriminator = "t" } }
    single { PlacementManager() }
    single { CaloriesManager() }
    single { FirstRunManager() }
    single { MajorUpdateManager() }
    single { MotdManager() }
    single { LadderManager() }
    single { LadderProgressManager() }
    single { TrialManager() }
    single { TrialSessionManager() }
    single { IgnoreListManager() }
    single { SongDataManager() }
    single { PlayerManager() }
}

expect val platformModule: Module

package com.perrigogames.life4

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import com.perrigogames.life4.api.*
import com.perrigogames.life4.api.base.LocalDataReader
import com.perrigogames.life4.api.base.LocalUncachedDataReader
import com.perrigogames.life4.db.GoalDatabaseHelper
import com.perrigogames.life4.db.ResultDatabaseHelper
import com.perrigogames.life4.feature.firstrun.FirstRunSettingsManager
import com.perrigogames.life4.feature.ladder.LadderGoalProgressManager
import com.perrigogames.life4.feature.motd.*
import com.perrigogames.life4.feature.placements.PlacementManager
import com.perrigogames.life4.feature.profile.UserRankManager
import com.perrigogames.life4.feature.settings.LadderListSelectionSettings
import com.perrigogames.life4.feature.settings.UserInfoSettings
import com.perrigogames.life4.feature.settings.UserRankSettings
import com.perrigogames.life4.feature.songlist.IgnoreListManager
import com.perrigogames.life4.feature.songlist.SongDataManager
import com.perrigogames.life4.feature.songresults.SongResultsManager
import com.perrigogames.life4.feature.trialrecords.TrialDatabaseHelper
import com.perrigogames.life4.feature.trialrecords.TrialRecordsManager
import com.perrigogames.life4.feature.trials.TrialManager
import com.perrigogames.life4.feature.trialsession.TrialSessionManager
import com.perrigogames.life4.ktor.GithubDataAPI
import com.perrigogames.life4.ktor.GithubDataImpl
import com.perrigogames.life4.ktor.Life4API
import com.perrigogames.life4.ktor.Life4APIImpl
import com.perrigogames.life4.model.*
import com.perrigogames.life4.model.mapping.LadderGoalMapper
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

typealias NativeInjectionFactory<T> = Scope.() -> T

fun initKoin(
    appModule: Module,
    extraAppModule: Module? = null,
    appDeclaration: KoinAppDeclaration = {}
) = startKoin {
    appDeclaration()
    modules(listOfNotNull(appModule, extraAppModule, platformModule, coreModule))
}.apply {
    // doOnStartup is a lambda which is implemented in Swift on iOS side
//    koin.get<() -> Unit>().invoke()
    // AppInfo is a Kotlin interface with separate Android and iOS implementations
//    koin.get<Logger> { parametersOf(null) }.also { kermit ->
//        kermit.v { "App Id ${koin.get<AppInfo>().appId}" }
//    }
}

val coreModule = module {
    single { GoalDatabaseHelper(get()) }
    single { ResultDatabaseHelper(get()) }
    single { TrialDatabaseHelper(get()) }

    single<GithubDataAPI> { GithubDataImpl() }
    single<Life4API> { Life4APIImpl(get()) }
    single { Json { classDiscriminator = "t" } }

    single { PlacementManager() }
    single { MajorUpdateManager() }
    single<MotdManager> { DefaultMotdManager() }
    single { LadderDataManager() }
    single { SongResultsManager() }
    single { LadderGoalProgressManager() }
    single { TrialManager() }
    single { TrialSessionManager() }
    single { TrialRecordsManager() }
    single { IgnoreListManager() }
    single { SongDataManager() }
    single { ChartResultOrganizer() }
    single { UserInfoSettings() }
    single { FirstRunSettingsManager() }
    single { UserRankSettings() }
    single { LadderListSelectionSettings() }
    single { UserRankManager() }
    single { GoalStateManager() }
    single { LadderGoalMapper() }
    single<MotdSettings> { DefaultMotdSettings() }

    // platformLogWriter() is a relatively simple config option, useful for local debugging. For production
    // uses you *may* want to have a more robust configuration from the native platform. In KaMP Kit,
    // that would likely go into platformModule expect/actual.
    // See https://github.com/touchlab/Kermit
    val baseLogger = Logger(config = StaticConfig(logWriterList = listOf(platformLogWriter())), "LIFE4")
    factory { (tag: String?) -> if (tag != null) baseLogger.withTag(tag) else baseLogger }
}

// Simple function to clean up the syntax a bit
fun KoinComponent.injectLogger(tag: String): Lazy<Logger> = inject { parametersOf(tag) }

expect val platformModule: Module

fun makeNativeModule(
    appInfo: AppInfo,
    ignoresReader: LocalDataReader,
    motdReader: LocalDataReader,
    partialDifficultyReader: LocalDataReader,
    placementsReader: LocalUncachedDataReader,
    ranksReader: LocalDataReader,
    songsReader: LocalDataReader,
    trialsReader: LocalDataReader,
    notifications: Notifications,
    additionalItems: Module.() -> Unit = {},
): Module {
    return module {
        single { appInfo }
        single { notifications }
        single(named(GithubDataAPI.IGNORES_FILE_NAME)) { ignoresReader }
        single(named(GithubDataAPI.MOTD_FILE_NAME)) { motdReader }
        single(named(GithubDataAPI.PARTIAL_DIFFICULTY_FILE_NAME)) { partialDifficultyReader }
        single(named(GithubDataAPI.PLACEMENTS_FILE_NAME)) { placementsReader }
        single(named(GithubDataAPI.RANKS_FILE_NAME)) { ranksReader }
        single(named(GithubDataAPI.SONGS_FILE_NAME)) { songsReader }
        single(named(GithubDataAPI.TRIALS_FILE_NAME)) { trialsReader }
        single { IgnoreListRemoteData() }
        single { LadderRemoteData() }
        single { MotdLocalRemoteData() }
        single { SongListRemoteData() }
        single { TrialRemoteData() }
        additionalItems()
    }
}
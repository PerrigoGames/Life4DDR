package com.perrigogames.life4.android

import android.app.Application
import android.content.Context
import com.perrigogames.life4.AppInfo
import com.perrigogames.life4.MR
import com.perrigogames.life4.android.util.AndroidNotifications
import com.perrigogames.life4.android.util.setupNotifications
import com.perrigogames.life4.feature.motd.MotdManager
import com.perrigogames.life4.feature.placements.PlacementManager
import com.perrigogames.life4.feature.songlist.SongDataManager
import com.perrigogames.life4.feature.trials.TrialManager
import com.perrigogames.life4.initKoin
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.IGNORES_FILE_NAME
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.MOTD_FILE_NAME
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.PARTIAL_DIFFICULTY_FILE_NAME
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.RANKS_FILE_NAME
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.SONGS_FILE_NAME
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.TRIALS_FILE_NAME
import com.perrigogames.life4.makeNativeModule
import com.perrigogames.life4.model.LadderDataManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.get


class Life4Application: Application() {

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            ManagerContainer().apply {
                ladderDataManager.onApplicationException()
                placementManager.onApplicationException()
                songDataManager.onApplicationException()
                trialManager.onApplicationException()
            }

            defaultHandler!!.uncaughtException(thread, exception)
        }

        initKoin(
            makeNativeModule(
                appInfo = AndroidAppInfo,
                ignoresReader = AndroidDataReader(MR.files.ignore_lists.rawResId, IGNORES_FILE_NAME),
                motdReader = AndroidDataReader(MR.files.motd.rawResId, MOTD_FILE_NAME),
                partialDifficultyReader = AndroidDataReader(MR.files.partial_difficulties.rawResId, PARTIAL_DIFFICULTY_FILE_NAME),
                placementsReader = AndroidUncachedDataReader(MR.files.placements.rawResId),
                ranksReader = AndroidDataReader(MR.files.ranks.rawResId, RANKS_FILE_NAME),
                songsReader = AndroidDataReader(MR.files.songs.rawResId, SONGS_FILE_NAME),
                trialsReader = AndroidDataReader(MR.files.trials.rawResId, TRIALS_FILE_NAME),
                notifications = AndroidNotifications(),
            ) {
                single<Context> { this@Life4Application }
//                single {
//                    { Log.i("Startup", "Hello from Android/Kotlin!") }
//                }
            }
        )

        setupNotifications(this)
        ManagerContainer() // instantiate the managers so that remote data gets pulled properly
    }

    private inner class ManagerContainer: KoinComponent {
        val ladderDataManager: LadderDataManager = get()
        val motdManager: MotdManager = get()
        val placementManager: PlacementManager = get()
        val songDataManager: SongDataManager = get()
        val trialManager: TrialManager = get()
    }
}

object AndroidAppInfo : AppInfo {
    override val appId: String = "LIFE4DDR" // FIXME
}

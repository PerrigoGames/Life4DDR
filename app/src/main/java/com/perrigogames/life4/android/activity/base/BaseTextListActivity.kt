package com.perrigogames.life4.android.activity.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.perrigogames.life4.android.databinding.ActivityBlockListCheckBinding
import com.perrigogames.life4.db.SongDatabaseHelper
import com.perrigogames.life4.db.aggregateDiffStyleString
import com.perrigogames.life4.enums.PlayStyle
import com.perrigogames.life4.model.IgnoreListManager
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseTextListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockListCheckBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockListCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textSongs.text = with(StringBuilder()) {
            buildText(this)
            toString()
        }
    }

    abstract fun buildText(builder: StringBuilder)
}

class BlockListCheckActivity: BaseTextListActivity(), KoinComponent {

    private val ignoreListManager: IgnoreListManager by inject()

    override fun buildText(builder: StringBuilder) {
        ignoreListManager.getCurrentlyIgnoredSongs().forEach {
            builder.append("(${it.id}) ${it.version} - ${it.title}\n")
        }
        ignoreListManager.getCurrentlyIgnoredCharts().forEach { entry ->
            entry.value.forEach { chart ->
                builder.append("(${entry.key.id}) ${entry.key.version} - ${entry.key.title} (${chart.aggregateDiffStyleString})\n")
            }
        }
    }
}

class SongRecordsListCheckActivity: BaseTextListActivity(), KoinComponent {

    private val songDb: SongDatabaseHelper by inject()

    override fun buildText(builder: StringBuilder) {
        songDb.allSongs().forEach { song ->
            val difficulties = songDb.selectChartsForSong(song.skillId, PlayStyle.SINGLE)
                .joinToString(separator = " / ") { "${it.difficultyNumber}" }
            builder.append("${song.title} - $difficulties\n")
        }
    }
}

package com.perrigogames.life4trials.data

import com.google.gson.annotations.SerializedName

class IgnoreLists(val lists: List<IgnoreList>)

/**
 * Data class to describe an ignore list, or a set of songs and
 * charts that do not appear in a particular game localization
 */
class IgnoreList(val id: String,
                 val name: String,
                 val songs: List<IgnoredSong>? = null,
                 val charts: List<IgnoredChart>? = null,
                 val mixes: List<GameVersion>)

class IgnoredSong(val title: String)

class IgnoredChart(val title: String,
                   @SerializedName("difficulty_class") val difficultyClass: DifficultyClass)
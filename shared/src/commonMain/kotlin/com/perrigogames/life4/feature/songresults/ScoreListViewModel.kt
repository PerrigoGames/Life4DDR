package com.perrigogames.life4.feature.songresults

import com.perrigogames.life4.enums.ClearType
import com.perrigogames.life4.enums.DifficultyClass
import com.perrigogames.life4.enums.colorRes
import com.perrigogames.life4.model.ChartResultOrganizer
import dev.icerock.moko.mvvm.flow.CStateFlow
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import dev.icerock.moko.mvvm.flow.cStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.icerock.moko.resources.ColorResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ScoreListViewModel: ViewModel(), KoinComponent {

    private val resultOrganizer: ChartResultOrganizer by inject()

    private val filterViewModel = FilterPanelViewModel()

    private val _state = MutableStateFlow(UIScoreList()).cMutableStateFlow()
    val state: CStateFlow<UIScoreList> = _state.cStateFlow()

    init {
        viewModelScope.launch {
            combine(
                filterViewModel.state,
                resultOrganizer.resultsForConfig(ScoreListContentConfig())
            ) { filterView, results ->
                UIScoreList(
                    scores = results.map { it.toUIScore() },
                    filter = filterView
                )
            }.collect(_state)
        }
    }

    fun handleFilterAction(action: UIFilterAction) {
        filterViewModel.handleAction(action)
    }
}

data class UIScoreList(
    val scores: List<UIScore> = emptyList(),
    val filter: UIFilterView = UIFilterView()
)

data class UIScore(
    val leftText: String = "",
    val rightText: String = "",
    val leftColor: ColorResource,
    val rightColor: ColorResource,
)

fun ChartResultPair.toUIScore() = UIScore(
    leftText = chart.song.title,
    rightText = (result?.score ?: 0).toString(),
    leftColor = chart.difficultyClass.colorRes,
    rightColor = (result?.clearType ?: ClearType.NO_PLAY).colorRes,
)

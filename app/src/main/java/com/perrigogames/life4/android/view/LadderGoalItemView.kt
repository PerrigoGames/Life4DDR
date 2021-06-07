package com.perrigogames.life4.android.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TableLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.perrigogames.life4.PlatformStrings
import com.perrigogames.life4.android.R
import com.perrigogames.life4.android.colorRes
import com.perrigogames.life4.android.databinding.MergeRankGoalV2Binding
import com.perrigogames.life4.android.databinding.RowSongDetailBinding
import com.perrigogames.life4.android.util.visibilityBool
import com.perrigogames.life4.data.BaseRankGoal
import com.perrigogames.life4.data.LadderGoalProgress
import com.perrigogames.life4.data.ProgressDetail.SongBreakdownDetail
import com.perrigogames.life4.data.ProgressDetail.TrialSuggestionDetail
import com.perrigogames.life4.db.GoalState
import com.perrigogames.life4.enums.ClearType
import com.perrigogames.life4.enums.GoalStatus.*
import com.perrigogames.life4.longNumberString
import org.koin.core.KoinComponent
import org.koin.core.inject

class LadderGoalItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

    private val platformStrings: PlatformStrings by inject()

    private val binding: MergeRankGoalV2Binding

    init {
        LayoutInflater.from(context).inflate(R.layout.merge_rank_goal_v2, this)
        binding = MergeRankGoalV2Binding.bind(this)
    }

    private var goal: BaseRankGoal? = null
    private var goalDB: GoalState? = null
    private var goalProgress: LadderGoalProgress? = null
    private var mandatory: Boolean = true
    private var oldColors: ColorStateList? = null

    var listener: LadderGoalItemListener? = null
    var expanded: Boolean = false

    private val currentState get() = goalDB?.status ?: INCOMPLETE

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.buttonStatusIcon.setOnClickListener { ifHasDB { g, db ->
            listener?.onStateToggle(this, g, db)
        } }
        binding.buttonIgnore.setOnClickListener { ifHasDB { g, db ->
            listener?.onIgnoreClicked(this, g, db)
        } }
        setOnClickListener { ifHasDB { g, db ->
            if (binding.tableExpandDetails.childCount > 0) {
                listener?.onExpandClicked(this, g, db)
            }
        } }
        setOnLongClickListener { ifHasDB { g, db ->
            listener?.onLongPressed(this, g, db)
        } }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.buttonStatusIcon.setOnClickListener(null)
        binding.buttonIgnore.setOnClickListener(null)
    }

    fun setGoal(
        goal: BaseRankGoal,
        goalDB: GoalState? = null,
        goalProgress: LadderGoalProgress? = null,
        mandatory: Boolean,
    ) {
        this.goal = goal
        this.goalDB = goalDB
        this.goalProgress = goalProgress
        this.mandatory = mandatory
        updateData()
    }

    fun setGoalState(goalDB: GoalState) {
        this.goalDB = goalDB
        updateData()
    }

    var canIgnore: Boolean = true
        set(v) {
            field = v
            updateIgnoreState()
        }

    private fun updateData() {
        if (oldColors == null) {
            oldColors = binding.textGoalSubtitle.textColors
        }

        binding.textGoalTitle.text = goal?.goalString(platformStrings) ?: ""
        binding.buttonStatusIcon.isChecked = currentState == COMPLETE

        updateExpand()
        updateProgress()
        updateIgnoreState()
    }

    private fun updateExpand() {
        binding.tableExpandDetails.children.forEach { pool.add(it.tag as RowSongDetailBinding) }
        binding.tableExpandDetails.removeAllViews()
        binding.tableExpandDetails.visibilityBool = expanded
    }

    private fun updateProgress() {
        goalProgress?.let {
            binding.textGoalSubtitle.text = if (!it.showMax) {
                it.progress.longNumberString()
            } else {
                context.getString(R.string.goal_progress_format, it.progress, it.max)
            }
            if (it.progress >= it.max) {
                binding.textGoalSubtitle.setTextColor(ContextCompat.getColor(context, R.color.gold))
            } else {
                binding.textGoalSubtitle.setTextColor(oldColors)
            }
            binding.progressAmount.apply {
                progress = it.progress
                max = it.max
            }

            when (val detail = it.detail) {
                is SongBreakdownDetail -> updateListDetails(detail)
                is TrialSuggestionDetail -> updateSuggestedTrials(detail)
            }
        }
        binding.textGoalSubtitle.visibilityBool = goalProgress?.let { it.progress != 0 && it.max != 0 } ?: false

        binding.progressAmount.visibilityBool = goalProgress != null
    }

    private fun updateListDetails(detail: SongBreakdownDetail) {
        detail.results.forEach { result ->
            val rowBinding = retrieveTableRowBinding(
                binding.tableExpandDetails,
                result.score.toInt().longNumberString(),
                result.title
            )
            if (result.clearType > ClearType.CLEAR) {
                rowBinding.textScore.setTextColor(ContextCompat.getColor(context, result.clearType.colorRes))
            } else {
                rowBinding.textScore.setTextColor(oldColors)
            }
            rowBinding.textTitle.setTextColor(ContextCompat.getColor(context, result.difficultyClass.colorRes))
            binding.tableExpandDetails.addView(rowBinding.root)
        }
    }

    private fun updateSuggestedTrials(trials: TrialSuggestionDetail) {

    }

    private fun updateIgnoreState() {
        binding.buttonIgnore.visibility = when {
            mandatory -> View.GONE // mandatory goals never show ignore, reclaim space
            canIgnore || currentState == IGNORED -> View.VISIBLE // show ignore if you're allowed to, or if you're already ignored
            else -> View.INVISIBLE // ignore function is hidden, don't reclaim space
        }
        binding.container.alpha = if (currentState == IGNORED) 0.3f else 1.0f
    }

    private inline fun ifHasDB(block: (BaseRankGoal, GoalState) -> Unit): Boolean {
        goal?.let { g ->
            goalDB?.let { db -> block(g, db) }
        }
        return true
    }

    interface LadderGoalItemListener {

        fun onStateToggle(itemView: LadderGoalItemView, item: BaseRankGoal, goalDB: GoalState)
        fun onIgnoreClicked(itemView: LadderGoalItemView, item: BaseRankGoal, goalDB: GoalState)
        fun onExpandClicked(itemView: LadderGoalItemView, item: BaseRankGoal, goalDB: GoalState)
        fun onLongPressed(itemView: LadderGoalItemView, item: BaseRankGoal, goalDB: GoalState)
    }

    companion object {

        private val pool = mutableListOf<RowSongDetailBinding>()
        private fun retrieveTableRowBinding(parent: TableLayout, scoreString: String, titleString: String): RowSongDetailBinding {
            val binding = if (pool.isNotEmpty())
                pool.removeAt(0)
            else RowSongDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            return binding.apply {
                textScore.text = scoreString
                textTitle.text = titleString
            }
        }
    }
}

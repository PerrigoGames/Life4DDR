package com.perrigogames.life4trials.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.perrigogames.life4trials.R
import com.perrigogames.life4trials.data.LadderRank
import kotlinx.android.synthetic.main.view_rank_header.view.*

/**
 * A special layout that displays a [RankImageView] and a corresponding label.
 */
class RankHeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        button_navigate_previous.setOnClickListener { navigationListener?.onPreviousClicked() }
        button_navigate_next.setOnClickListener { navigationListener?.onNextClicked() }
    }

    var rank: LadderRank? = null
        set(v) {
            field = v
            image_rank.rank = v
            updateTitle()
        }

    var navigationListener: NavigationListener? = null
        set(v) {
            field = v
            button_navigate_previous.visibility = if (v != null) VISIBLE else GONE
            button_navigate_next.visibility = if (v != null) VISIBLE else GONE
        }

    var genericTitles = false
        set(v) {
            field = v
            updateTitle()
        }

    fun setIconSize(size: Int) {
        image_rank.apply {
            layoutParams = layoutParams.also {
                it.width = size
                it.height = size
            }
        }
    }

    fun navigationButtonClicked(v: View) {
        val prev = v.id == R.id.button_navigate_previous
        if (prev) navigationListener?.onPreviousClicked() else
            navigationListener?.onNextClicked()
    }

    private fun updateTitle() {
        text_rank_title.text = rank?.let { context.getString(if (genericTitles) it.groupNameRes else it.nameRes) } ?: ""
    }

    interface NavigationListener {

        fun onPreviousClicked()
        fun onNextClicked()
    }
}
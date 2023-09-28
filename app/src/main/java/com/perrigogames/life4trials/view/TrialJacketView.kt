package com.perrigogames.life4trials.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import androidx.constraintlayout.widget.ConstraintSet.TOP
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.perrigogames.life4trials.R
import com.perrigogames.life4trials.activity.SettingsActivity
import com.perrigogames.life4trials.data.Trial
import com.perrigogames.life4trials.data.TrialRank
import com.perrigogames.life4trials.util.SharedPrefsUtil
import com.perrigogames.life4trials.util.visibilityBool
import kotlinx.android.synthetic.main.view_trial_jacket.view.*

class TrialJacketView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    var tintOnRank: TrialRank? = null
        set(v) {
            field = v
            updateRankTint()
        }

    var trial: Trial? = null
        set(v) {
            field = v
            text_trial_title.text = v?.name
            image_trial_difficulty.visibilityBool = v?.difficulty != null
            text_trial_difficulty.visibilityBool = v?.difficulty != null
            text_trial_difficulty.text = v?.difficulty?.toString()
            text_trial_title.visibility = View.GONE
            if (v != null) {
                val resId = v.jacketResId(context)
                if (resId == R.drawable.trial_default) {
                    text_trial_title.visibility = View.VISIBLE
                }
                image_trial_jacket.setImageDrawable(ContextCompat.getDrawable(context, resId))
            }
        }

    var rank: TrialRank? = null
        set(v) {
            field = v
            if (rank != null) {
                image_badge_highest.setImageDrawable(ContextCompat.getDrawable(context, rank!!.drawableRes))
                image_badge_highest_center.setImageDrawable(ContextCompat.getDrawable(context, rank!!.drawableRes))
            }
            updateRankTint()
        }

    var exScore: Int? = null
        set(v) {
            field = v
            updateExScore()
        }

    private val shouldTint: Boolean
        get() = rank != null && rank == tintOnRank

    private fun updateRankTint() {
        if (shouldTint) {
            view_foreground_tint.visibility = VISIBLE
            view_foreground_tint.setImageDrawable(ColorDrawable(ResourcesCompat.getColor(context.resources, rank!!.colorRes, context.theme)))
            image_badge_highest.visibility = GONE
            image_badge_highest_center.visibility = VISIBLE
            text_ex_score.visibility = GONE
            text_ex_score_center.visibility = VISIBLE
        } else {
            view_foreground_tint.visibility = GONE
            image_badge_highest.visibilityBool = rank != null
            image_badge_highest_center.visibility = GONE
            text_ex_score.visibility = VISIBLE
            text_ex_score_center.visibility = GONE
        }
        updateExScore()
    }

    private fun updateExScore() {
        if (exScore != null) {
            if (shouldTint) {
                text_ex_score_center.visibility = VISIBLE
            } else {
                text_ex_score.visibility = VISIBLE
            }
            if (trial != null && trial!!.total_ex != null && SharedPrefsUtil.getUserFlag(context, SettingsActivity.KEY_LIST_SHOW_EX_REMAINING, false)) {
                text_ex_score.text = context.getString(R.string.ex_score_missing_newline_string_format, exScore, exScore!! - trial!!.total_ex!!)
                text_ex_score_center.text = context.getString(R.string.ex_score_missing_string_format, exScore, exScore!! - trial!!.total_ex!!)
            } else {
                text_ex_score.text = context.getString(R.string.ex_score_string_format, exScore!!)
                text_ex_score_center.text = context.getString(R.string.ex_score_string_format, exScore!!)
            }
        } else {
            text_ex_score.visibility = GONE
            text_ex_score_center.visibility = GONE
        }
        ConstraintSet().also {
            it.clone(this)
            if (exScore != null) {
                it.connect(image_badge_highest_center.id, BOTTOM, text_ex_score_center.id, TOP, 0)
            } else {
                it.connect(image_badge_highest_center.id, BOTTOM, this.id, BOTTOM, 0)
            }
            it.applyTo(this)
        }
    }
}
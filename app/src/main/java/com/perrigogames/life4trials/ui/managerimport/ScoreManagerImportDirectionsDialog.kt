package com.perrigogames.life4trials.ui.managerimport

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.perrigogames.life4trials.R
import com.perrigogames.life4trials.activity.SettingsActivity.Companion.KEY_IMPORT_SKIP_DIRECTIONS
import com.perrigogames.life4trials.life4app
import kotlinx.android.synthetic.main.dialog_manager_import_directions.view.*

class ScoreManagerImportDirectionsDialog(var listener: Listener? = null): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireActivity().let {
            val view = it.layoutInflater.inflate(R.layout.dialog_manager_import_directions, null)
            view.check_dont_show.setOnCheckedChangeListener { _, checked ->
                it.life4app.settingsManager.setUserFlag(KEY_IMPORT_SKIP_DIRECTIONS, checked)
            }
            AlertDialog.Builder(it)
                .setView(view)
                .setPositiveButton(R.string.copy_and_continue) { _, _ -> listener?.onCopyAndContinue() }
                .setOnCancelListener { listener?.onDialogCancelled() }
                .create()
        }
    }

    interface Listener {
        fun onDialogCancelled()
        fun onCopyAndContinue()
    }

    companion object {
        const val TAG = "ImportEntryDialog"
    }
}
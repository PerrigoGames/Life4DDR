package com.perrigogames.life4trials.ui.unlocks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.perrigogames.life4trials.life4app

class SongUnlockFragment: Fragment() {

    private lateinit var adapter: SongUnlockAdapter

    private val ignoreListManager get() = context!!.life4app.ignoreListManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        adapter = SongUnlockAdapter().apply {
            listener = { item, selection ->
                Log.v("unlocks", "${item.name}: $selection")
                ignoreListManager.setGroupUnlockState(item.id, selection)
            }
            ignoreGroups = ignoreListManager.selectedIgnoreGroups ?: emptyList()
            selectionReader = { id -> ignoreListManager.getGroupUnlockFlags(id)!! }
        }
        return RecyclerView(inflater.context).apply {
            layoutManager = LinearLayoutManager(inflater.context)
            adapter = this@SongUnlockFragment.adapter
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }
    }
}
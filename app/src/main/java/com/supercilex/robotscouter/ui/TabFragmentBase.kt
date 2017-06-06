package com.supercilex.robotscouter.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.RobotScouter
import com.supercilex.robotscouter.util.getTabKey
import com.supercilex.robotscouter.util.restoreRecyclerViewState
import com.supercilex.robotscouter.util.saveRecyclerViewState

abstract class TabFragmentBase : Fragment() {
    protected abstract val rootView: View
    protected abstract val adapter: FirebaseRecyclerAdapter<*, *>

    protected val key by lazy { getTabKey(arguments)!! }
    protected val recyclerView: RecyclerView by lazy {
        rootView.findViewById<RecyclerView>(R.id.list)
    }
    protected val manager by lazy { LinearLayoutManager(context) }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        recyclerView.layoutManager = manager
        recyclerView.setHasFixedSize(true)

        recyclerView.adapter = adapter
        restoreRecyclerViewState(savedInstanceState, manager)

        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) = saveRecyclerViewState(outState, manager)

    override fun onPause() {
        super.onPause()
        recyclerView.clearFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.cleanup()
        RobotScouter.getRefWatcher(activity).watch(this)
    }
}

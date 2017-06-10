package com.supercilex.robotscouter.ui.scouting.scout

import android.support.v7.widget.RecyclerView
import android.view.View
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.ui.scouting.TabFragmentBase
import com.supercilex.robotscouter.util.getScoutMetricsRef
import com.supercilex.robotscouter.util.getTabKeyBundle

class ScoutFragment : TabFragmentBase() {
    override val rootView by lazy {
        View.inflate(context, R.layout.fragment_scout, null) as RecyclerView
    }
    override val adapter by lazy {
        ScoutAdapter(getScoutMetricsRef(key), childFragmentManager, recyclerView)
    }

    companion object {
        fun newInstance(scoutKey: String) =
                ScoutFragment().apply { arguments = getTabKeyBundle(scoutKey) }
    }
}

package com.supercilex.robotscouter.ui.scouting.scout

import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.data.util.TeamHelper
import com.supercilex.robotscouter.ui.scouting.PagerAdapterBase
import com.supercilex.robotscouter.util.FIREBASE_SCOUTS
import com.supercilex.robotscouter.util.SINGLE_ITEM
import com.supercilex.robotscouter.util.deleteScout
import com.supercilex.robotscouter.util.getScoutIndicesRef
import com.supercilex.robotscouter.util.isOffline

class ScoutPagerAdapter(private val appBarViewHolder: AppBarViewHolderBase,
                        private val teamHelper: TeamHelper,
                        fragment: Fragment,
                        tabLayout: TabLayout,
                        currentTabKey: String?) :
        PagerAdapterBase(fragment, tabLayout, getScoutIndicesRef(teamHelper.team.key), currentTabKey) {
    override val rootRef = FIREBASE_SCOUTS

    fun onScoutDeleted() {
        val index = keys.indexOf(currentTabKey)
        var newKey: String? = null
        if (keys.size > SINGLE_ITEM) {
            newKey = if (keys.size - 1 > index) keys[index + 1] else keys[index - 1]
        }
        deleteScout(query.ref.key, currentTabKey!!)
        currentTabKey = newKey
    }

    override fun onStateChange(hadScouts: Boolean) {
        super.onStateChange(hadScouts)
        if (hadScouts && keys.isEmpty() && !isOffline(fragment.context) && fragment.isResumed) {
            ShouldDeleteTeamDialog.show(fragment.childFragmentManager, teamHelper)
        }
        appBarViewHolder.setDeleteScoutMenuItemVisible(!keys.isEmpty())
    }

    override fun getItem(position: Int) = ScoutFragment.newInstance(keys[position])

    override fun getPageTitle(position: Int): String =
            fragment.getString(R.string.title_scout_tab, count - position)
}

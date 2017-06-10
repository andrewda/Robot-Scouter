package com.supercilex.robotscouter.ui.scouting

import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.RobotScouter
import com.supercilex.robotscouter.util.FIREBASE_NAME
import java.util.ArrayList

abstract class PagerAdapterBase(protected val fragment: Fragment,
                                private val tabLayout: TabLayout,
                                protected val query: Query,
                                var currentTabKey: String?) :
        FragmentStatePagerAdapter(fragment.childFragmentManager),
        ValueEventListener, TabLayout.OnTabSelectedListener, View.OnLongClickListener {
    protected abstract val rootRef: DatabaseReference

    protected val keys = ArrayList<String>()

    private val tabNameListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val tabIndex = keys.indexOf(snapshot.ref.parent.key)

            tabLayout.getTabAt(tabIndex)!!.text =
                    if (snapshot.value == null) getPageTitle(tabIndex)
                    else snapshot.getValue(String::class.java)

            val tabView = (tabLayout.getChildAt(0) as LinearLayout).getChildAt(tabIndex)
            tabView.setOnLongClickListener(this@PagerAdapterBase)
            tabView.id = tabIndex
        }

        override fun onCancelled(error: DatabaseError) = this@PagerAdapterBase.onCancelled(error)
    }

    init {
        query.addValueEventListener(this)
    }

    fun cleanup() {
        query.removeEventListener(this)
        removeNameListeners()
        RobotScouter.getRefWatcher(fragment.activity).watch(this)
    }

    protected open fun onStateChange(hadScouts: Boolean) {
        fragment.view!!.findViewById<View>(R.id.no_content_hint).visibility =
                if (keys.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun getCount() = keys.size

    override fun getItemPosition(any: Any?) = PagerAdapter.POSITION_NONE

    override fun onTabSelected(tab: TabLayout.Tab) {
        currentTabKey = keys[tab.position]
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        removeNameListeners()
        val hadScouts = !keys.isEmpty()
        keys.clear()
        for (scoutIndex in snapshot.children) {
            val key = scoutIndex.key
            keys.add(0, key)
            getTabNameRef(key).addValueEventListener(tabNameListener)
        }

        onStateChange(hadScouts)

        tabLayout.removeOnTabSelectedListener(this)
        notifyDataSetChanged()
        tabLayout.addOnTabSelectedListener(this)

        if (!keys.isEmpty()) {
            if (TextUtils.isEmpty(currentTabKey)) {
                selectTab(0)
                currentTabKey = keys[0]
            } else {
                selectTab(keys.indexOf(currentTabKey))
            }
        }
    }

    private fun selectTab(index: Int) = tabLayout.getTabAt(index)?.select()

    private fun getTabNameRef(key: String) = rootRef.child(key).child(FIREBASE_NAME)

    private fun removeNameListeners() {
        for (key in keys) getTabNameRef(key).removeEventListener(tabNameListener)
    }

    override fun onLongClick(v: View): Boolean {
        TabNameDialog.show(
                fragment.childFragmentManager,
                rootRef.child(keys[v.id]).child(FIREBASE_NAME),
                tabLayout.getTabAt(v.id)!!.text!!.toString())
        return true
    }

    override fun onCancelled(error: DatabaseError) = FirebaseCrash.report(error.toException())

    override fun onTabUnselected(tab: TabLayout.Tab) = Unit

    override fun onTabReselected(tab: TabLayout.Tab) = Unit
}

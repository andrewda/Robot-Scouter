package com.supercilex.robotscouter.ui.template

import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.data.model.Metric
import com.supercilex.robotscouter.data.util.TeamHelper
import com.supercilex.robotscouter.ui.TabFragmentBase
import com.supercilex.robotscouter.ui.teamlist.OnBackPressedListener
import com.supercilex.robotscouter.util.FIREBASE_TEMPLATES
import com.supercilex.robotscouter.util.FIREBASE_VALUE
import com.supercilex.robotscouter.util.getHighestIntPriority
import com.supercilex.robotscouter.util.getTabKeyBundle
import com.supercilex.robotscouter.util.getTemplateMetricsRef
import java.util.Collections

class TemplateFragment : TabFragmentBase(), View.OnClickListener, OnBackPressedListener {
    override val rootView: View by lazy {
        View.inflate(context, R.layout.fragment_template, null)
    }
    override val adapter by lazy {
        TemplateAdapter(
                getTemplateMetricsRef(key),
                childFragmentManager,
                recyclerView,
                itemTouchCallback)
    }
    private val itemTouchCallback by lazy { ScoutTemplateItemTouchCallback(rootView) }
    private val fam: FloatingActionMenu by lazy {
        fun initFab(fab: FloatingActionButton, @DrawableRes icon: Int) {
            fab.setOnClickListener(this)
            fab.setImageResource(icon)
        }

        initFab(rootView.findViewById(R.id.add_header), R.drawable.ic_title_white_24dp)
        initFab(rootView.findViewById(R.id.add_checkbox), R.drawable.ic_done_white_24dp)
        initFab(rootView.findViewById(R.id.add_stopwatch), R.drawable.ic_timer_white_24dp)
        initFab(rootView.findViewById(R.id.add_note), R.drawable.ic_note_white_24dp)
        initFab(rootView.findViewById(R.id.add_counter), R.drawable.ic_count_white_24dp)
        initFab(rootView.findViewById(R.id.add_spinner), R.drawable.ic_list_white_24dp)

        // This lets us close the fam when the recyclerview it touched
        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                fam.close(true)
                return false
            }
        })

        rootView.findViewById<FloatingActionMenu>(R.id.fab_menu)
    }

    private var hasAddedItem: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fam

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchCallback.setItemTouchHelper(itemTouchHelper)
        itemTouchCallback.setAdapter(adapter)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    // User scrolled down -> hide the FAB
                    fam.hideMenuButton(true)
                } else if (dy < 0) {
                    fam.showMenuButton(true)
                } else if (hasAddedItem &&
                        (manager.findFirstCompletelyVisibleItemPosition() != 0
                                || manager.findLastCompletelyVisibleItemPosition() != adapter.itemCount - 1)) {
                    fam.hideMenuButton(true)
                }

                hasAddedItem = false
            }
        })

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
            inflater.inflate(R.menu.template_options, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_reset_template_all, R.id.action_reset_template_team -> {
                recyclerView.clearFocus()
                ResetTemplateDialog.show(
                        childFragmentManager,
                        TeamHelper.parse(arguments),
                        id == R.id.action_reset_template_all)
            }
            R.id.action_remove_metrics -> {
                RemoveAllMetricsDialog.show(childFragmentManager, FIREBASE_TEMPLATES.child(key))
            }
            else -> return false
        }
        return true
    }

    override fun onClick(v: View) {
        val id = v.id

        val priority = getHighestIntPriority(adapter.snapshots) + 1
        val metricRef = getTemplateMetricsRef(key).push()
        when (id) {
            R.id.add_checkbox -> metricRef.setValue(Metric.Boolean(""), priority)
            R.id.add_counter -> metricRef.setValue(Metric.Number(""), priority)
            R.id.add_spinner -> {
                metricRef.setValue(
                        Metric.List("", Collections.singletonMap("a", "Item 1"), "a"),
                        priority)
                metricRef.child(FIREBASE_VALUE).child("a").setPriority(0)
            }
            R.id.add_note -> metricRef.setValue(Metric.Text(""), priority)
            R.id.add_stopwatch -> metricRef.setValue(Metric.Stopwatch("", emptyList()), priority)
            R.id.add_header -> metricRef.setValue(Metric.Header(""), priority)
            else -> throw IllegalStateException("Unknown id: $id")
        }

        itemTouchCallback.addItemToScrollQueue(adapter.itemCount)
        fam.close(true)
        hasAddedItem = true
    }

    override fun onBackPressed(): Boolean = if (fam.isOpened) {
        fam.close(true); true
    } else {
        false
    }

    companion object {
        fun newInstance(templateKey: String) =
                TemplateFragment().apply { arguments = getTabKeyBundle(templateKey) }
    }
}

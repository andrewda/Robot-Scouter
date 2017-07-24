package com.supercilex.robotscouter.ui.scout

import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.Query
import com.supercilex.robotscouter.data.model.HEADER
import com.supercilex.robotscouter.data.model.Metric
import com.supercilex.robotscouter.data.model.MetricType
import com.supercilex.robotscouter.ui.CardListHelper
import com.supercilex.robotscouter.ui.MetricViewHolderBase
import com.supercilex.robotscouter.util.METRIC_PARSER

abstract class ScoutAdapterBase(query: Query,
                                private val manager: FragmentManager,
                                private val recyclerView: RecyclerView) :
        FirebaseRecyclerAdapter<Metric<*>, MetricViewHolderBase<*, *, *>>(
                METRIC_PARSER,
                0,
                MetricViewHolderBase::class.java,
                query) {
    protected abstract val cardListHelper: CardListHelper
    private val animator: SimpleItemAnimator = recyclerView.itemAnimator as SimpleItemAnimator

    override fun populateViewHolder(viewHolder: MetricViewHolderBase<*, *, *>,
                                    metric: Metric<*>,
                                    position: Int) {
        animator.supportsChangeAnimations = true

        cardListHelper.onBind(viewHolder)

        @Suppress("UNCHECKED_CAST")
        viewHolder as MetricViewHolderBase<Metric<Any>, *, *>
        @Suppress("UNCHECKED_CAST")
        metric as Metric<Any>
        viewHolder.bind(metric, manager, animator)
    }

    @MetricType
    override fun getItemViewType(position: Int): Int = getItem(position).type

    protected inner class ListHelper(hasSafeCorners: Boolean = false) :
            CardListHelper(this, recyclerView, hasSafeCorners) {
        override fun isFirstItem(position: Int): Boolean =
                super.isFirstItem(position) || isHeader(position)

        override fun isLastItem(position: Int): Boolean =
                super.isLastItem(position) || isHeader(position + 1)

        private fun isHeader(position: Int): Boolean = getItem(position).type == HEADER
    }
}

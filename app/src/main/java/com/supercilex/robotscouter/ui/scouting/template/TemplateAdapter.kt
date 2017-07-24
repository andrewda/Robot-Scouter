package com.supercilex.robotscouter.ui.scouting.template

import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.database.ChangeEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Query
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.data.model.BOOLEAN
import com.supercilex.robotscouter.data.model.HEADER
import com.supercilex.robotscouter.data.model.LIST
import com.supercilex.robotscouter.data.model.Metric
import com.supercilex.robotscouter.data.model.MetricType
import com.supercilex.robotscouter.data.model.NUMBER
import com.supercilex.robotscouter.data.model.STOPWATCH
import com.supercilex.robotscouter.data.model.TEXT
import com.supercilex.robotscouter.ui.CardListHelper
import com.supercilex.robotscouter.ui.scouting.MetricAdapterBase
import com.supercilex.robotscouter.ui.scouting.MetricViewHolderBase
import com.supercilex.robotscouter.ui.scouting.template.viewholder.CheckboxTemplateViewHolder
import com.supercilex.robotscouter.ui.scouting.template.viewholder.CounterTemplateViewHolder
import com.supercilex.robotscouter.ui.scouting.template.viewholder.EditTextTemplateViewHolder
import com.supercilex.robotscouter.ui.scouting.template.viewholder.HeaderTemplateViewHolder
import com.supercilex.robotscouter.ui.scouting.template.viewholder.SpinnerTemplateViewHolder
import com.supercilex.robotscouter.ui.scouting.template.viewholder.StopwatchTemplateViewHolder

class TemplateAdapter(query: Query,
                      manager: FragmentManager,
                      recyclerView: RecyclerView,
                      private val callback: TemplateItemTouchCallback) :
        MetricAdapterBase(query, manager, recyclerView) {
    override val cardListHelper: CardListHelper = ListHelper()

    init {
        callback.setCardListHelper(cardListHelper)
    }

    override fun populateViewHolder(viewHolder: MetricViewHolderBase<*, *, *>,
                                    metric: Metric<*>,
                                    position: Int) {
        super.populateViewHolder(viewHolder, metric, position)
        callback.onBind(viewHolder, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, @MetricType viewType: Int):
            MetricViewHolderBase<*, *, *> {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        when (viewType) {
            BOOLEAN -> return CheckboxTemplateViewHolder(
                    inflater.inflate(R.layout.scout_template_checkbox, parent, false))
            NUMBER -> return CounterTemplateViewHolder(
                    inflater.inflate(R.layout.scout_template_counter, parent, false))
            TEXT -> return EditTextTemplateViewHolder(
                    inflater.inflate(R.layout.scout_template_notes, parent, false))
            LIST -> return SpinnerTemplateViewHolder(
                    inflater.inflate(R.layout.scout_template_spinner, parent, false))
            STOPWATCH -> return StopwatchTemplateViewHolder(
                    inflater.inflate(R.layout.scout_template_stopwatch, parent, false))
            HEADER -> return HeaderTemplateViewHolder(
                    inflater.inflate(R.layout.scout_template_header, parent, false))
            else -> throw IllegalStateException()
        }
    }

    override fun onChildChanged(type: ChangeEventListener.EventType,
                                snapshot: DataSnapshot?,
                                index: Int,
                                oldIndex: Int) {
        if (callback.onChildChanged(type, index)) {
            super.onChildChanged(type, snapshot, index, oldIndex)
        }
    }
}

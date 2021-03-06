package com.supercilex.robotscouter.ui.scout.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.data.model.Metric

open class HeaderViewHolder(itemView: View) : ScoutViewHolderBase<Metric.Header, Nothing?, TextView>(itemView) {
    private val topMargin =
            itemView.resources.getDimension(R.dimen.list_item_padding_vertical_within).toInt()

    override fun bind() {
        super.bind()
        (itemView.layoutParams as RecyclerView.LayoutParams).topMargin =
                if (layoutPosition == 0) 0 else topMargin
    }
}

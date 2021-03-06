package com.supercilex.robotscouter.ui.scout.viewholder.template

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.data.model.Metric
import com.supercilex.robotscouter.ui.scout.viewholder.ScoutViewHolderBase

class EditTextTemplateViewHolder(itemView: View) :
        ScoutViewHolderBase<Metric.Text, String?, TextView>(itemView), ScoutTemplateViewHolder {
    private val text: EditText = itemView.findViewById(R.id.text)

    override fun bind() {
        super.bind()
        text.setText(metric.value)
        name.onFocusChangeListener = this
        text.onFocusChangeListener = this
    }

    override fun requestFocus() {
        name.requestFocus()
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (!hasFocus && v.id == R.id.name) updateMetricName(name.text.toString())
        if (!hasFocus && v.id == R.id.text) updateMetricValue(text.text.toString())
    }
}

package com.supercilex.robotscouter.ui.template

import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.ui.PagerAdapterBase
import com.supercilex.robotscouter.util.FIREBASE_TEMPLATES
import com.supercilex.robotscouter.util.templateIndicesRef

class TemplatePagerAdapter(fragment: Fragment, tabLayout: TabLayout) :
        PagerAdapterBase(fragment, tabLayout, templateIndicesRef, null) { // TODO
    override val rootRef = FIREBASE_TEMPLATES

    override fun getItem(position: Int) = TemplateFragment.newInstance(keys[position])

    override fun getPageTitle(position: Int): String =
            fragment.getString(R.string.title_template_tab, count - position)
}

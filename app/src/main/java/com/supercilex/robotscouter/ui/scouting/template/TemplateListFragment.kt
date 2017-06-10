package com.supercilex.robotscouter.ui.scouting.template

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.ui.teamlist.OnBackPressedListener
import com.supercilex.robotscouter.util.getTabKey
import com.supercilex.robotscouter.util.getTabKeyBundle

class TemplateListFragment : Fragment(), OnBackPressedListener {
    private val rootView by lazy { View.inflate(context, R.layout.fragment_template_list, null) }
    private val pagerAdapter by lazy {
        val tabLayout = rootView.findViewById<TabLayout>(R.id.tabs)
        val viewPager = rootView.findViewById<ViewPager>(R.id.viewpager)
        val adapter = TemplatePagerAdapter(this, tabLayout)

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        adapter
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        pagerAdapter.currentTabKey = if (savedInstanceState == null) {
            getTabKey(arguments)
        } else {
            getTabKey(savedInstanceState)
        }

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(rootView.findViewById(R.id.toolbar))
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed(): Boolean =
            childFragmentManager.fragments.any { it is OnBackPressedListener && it.onBackPressed() }

    companion object {
        const val TAG = "TemplateListFragment"

        fun newInstance(templateKey: String?) =
                TemplateListFragment().apply { arguments = getTabKeyBundle(templateKey) }
    }

    // TODO
//    private fun getTemplateKey() {
//        val teamHelper = TeamHelper.parse(arguments)
//        mTemplateKey = teamHelper.team.templateKey
//
//        if (TextUtils.isEmpty(mTemplateKey)) {
//            cancelAllDownloadTeamDataJobs(context)
//
//            if (!Constants.sFirebaseTemplates.isEmpty()) {
//                mTemplateKey = Constants.sFirebaseTemplates[0].key
//                teamHelper.updateTemplateKey(mTemplateKey)
//                return
//            }
//
//            val newTemplateRef = FIREBASE_TEMPLATES.push()
//            mTemplateKey = newTemplateRef.getKey()
//
//            FirebaseCopier.copyTo(Constants.sDefaultTemplate, newTemplateRef)
//            teamHelper.updateTemplateKey(mTemplateKey)
//            templateIndicesRef.child(mTemplateKey).setValue(true)
//
//            for (i in Constants.sFirebaseTeams.indices) {
//                val team = Constants.sFirebaseTeams.getObject(i)
//                val templateKey = team.templateKey
//
//                if (TextUtils.isEmpty(templateKey)) {
//                    team.helper.updateTemplateKey(mTemplateKey)
//                }
//            }
//        }
//    }
}

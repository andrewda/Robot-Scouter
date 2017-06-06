package com.supercilex.robotscouter.ui.template

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.util.TAB_KEY

class TemplateEditorActivity : AppCompatActivity() {
    private val templateListFragment: TemplateListFragment by lazy {
        supportFragmentManager.findFragmentByTag(TemplateListFragment.TAG) as TemplateListFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.RobotScouter_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template_editor)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.template_list,
                            TemplateListFragment.newInstance(intent.getStringExtra(TAB_KEY)),
                            TemplateListFragment.TAG)
                    .commit()
        }
    }

    override fun onBackPressed() {
        if (!templateListFragment.onBackPressed()) super.onBackPressed()
    }

    companion object {
        fun start(context: Context, templateKey: String? = null) =
                context.startActivity(Intent(context, TemplateEditorActivity::class.java)
                        .putExtra(TAB_KEY, templateKey))
    }
}

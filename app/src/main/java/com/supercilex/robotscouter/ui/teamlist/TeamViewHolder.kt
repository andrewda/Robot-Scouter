package com.supercilex.robotscouter.ui.teamlist

import android.support.annotation.Keep
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.data.model.Team
import com.supercilex.robotscouter.ui.TeamDetailsDialog
import com.supercilex.robotscouter.ui.scout.ScoutListFragmentBase
import com.supercilex.robotscouter.util.animateCircularReveal
import de.hdodenhof.circleimageview.CircleImageView

class TeamViewHolder @Keep constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
    private val mediaImageView: CircleImageView = itemView.findViewById<CircleImageView>(R.id.media)
    private val numberTextView: TextView = itemView.findViewById<TextView>(R.id.number)
    private val nameTextView: TextView = itemView.findViewById<TextView>(R.id.name)
    private val newScoutButton: ImageButton = itemView.findViewById<ImageButton>(R.id.new_scout)

    private lateinit var team: Team
    private lateinit var fragment: Fragment
    private lateinit var menuManager: TeamMenuManager
    private var isItemSelected: Boolean = false
    private var couldItemBeSelected: Boolean = false
    private var isScouting: Boolean = false

    fun bind(team: Team,
             fragment: Fragment,
             menuManager: TeamMenuManager,
             isItemSelected: Boolean,
             couldItemBeSelected: Boolean,
             isScouting: Boolean) {
        this.team = team
        this.fragment = fragment
        this.menuManager = menuManager
        this.isItemSelected = isItemSelected
        this.couldItemBeSelected = couldItemBeSelected
        this.isScouting = isScouting

        setTeamNumber()
        setTeamName()
        updateItemStatus()

        mediaImageView.setOnClickListener(this)
        mediaImageView.setOnLongClickListener(this)
        newScoutButton.setOnClickListener(this)
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
    }

    private fun updateItemStatus() {
        if (isItemSelected) {
            Glide.with(itemView.context)
                    .load("")
                    .placeholder(ContextCompat.getDrawable(
                            itemView.context, R.drawable.ic_check_circle_grey_144dp))
                    .into(mediaImageView)
        } else {
            setTeamMedia()
        }

        animateCircularReveal(newScoutButton, !couldItemBeSelected)
        itemView.isActivated = !isItemSelected && !couldItemBeSelected && isScouting
        itemView.isSelected = isItemSelected
    }

    private fun setTeamNumber() {
        numberTextView.text = team.number
    }

    private fun setTeamName() = if (TextUtils.isEmpty(team.name)) {
        nameTextView.text = itemView.context.getString(R.string.unknown_team)
    } else {
        nameTextView.text = team.name
    }

    private fun setTeamMedia() = Glide.with(itemView.context)
            .load(team.media)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(R.drawable.ic_memory_grey_48dp)
            .into(mediaImageView)

    override fun onClick(v: View) {
        if (v.id == R.id.media || isItemSelected || couldItemBeSelected) {
            onTeamContextMenuRequested()
        } else {
            (itemView.context as TeamSelectionListener).onTeamSelected(
                    ScoutListFragmentBase.getBundle(team, v.id == R.id.new_scout, null), false)
        }
    }

    override fun onLongClick(v: View): Boolean {
        if (isItemSelected || couldItemBeSelected || v.id == R.id.root) {
            onTeamContextMenuRequested()
            return true
        } else if (v.id == R.id.media) {
            TeamDetailsDialog.show(fragment.childFragmentManager, team.helper)
            return true
        }

        return false
    }

    private fun onTeamContextMenuRequested() {
        isItemSelected = !isItemSelected
        updateItemStatus()
        menuManager.onTeamContextMenuRequested(team.helper)
    }

    override fun toString(): String = team.toString()
}
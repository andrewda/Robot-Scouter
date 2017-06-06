package com.supercilex.robotscouter.data.util

import android.text.TextUtils
import com.google.firebase.database.DataSnapshot
import com.supercilex.robotscouter.data.model.User
import com.supercilex.robotscouter.util.FIREBASE_TEAM_INDICES
import com.supercilex.robotscouter.util.FIREBASE_USERS
import com.supercilex.robotscouter.util.getTemplateIndicesRef
import com.supercilex.robotscouter.util.templateIndicesRef

data class UserHelper(private val user: User) {
    fun add() = FIREBASE_USERS.child(user.uid).setValue(user).continueWith { null }

    fun transferData(prevUid: String?) {
        if (TextUtils.isEmpty(prevUid)) return
        prevUid!!

        val prevTeamRef = FIREBASE_TEAM_INDICES.child(prevUid)
        object : FirebaseCopier(prevTeamRef, TeamHelper.getIndicesRef()) {
            override fun onDataChange(snapshot: DataSnapshot) {
                super.onDataChange(snapshot)
                prevTeamRef.removeValue()
            }
        }.performTransformation()

        val prevScoutTemplatesRef = getTemplateIndicesRef(prevUid)
        object : FirebaseCopier(prevScoutTemplatesRef, templateIndicesRef) {
            override fun onDataChange(snapshot: DataSnapshot) {
                super.onDataChange(snapshot)
                prevScoutTemplatesRef.removeValue()
            }
        }.performTransformation()
    }

    override fun toString() = user.toString()
}

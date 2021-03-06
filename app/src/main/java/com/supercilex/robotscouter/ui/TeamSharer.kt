package com.supercilex.robotscouter.ui

import android.content.Intent
import android.net.Uri
import android.support.annotation.Size
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.view.View
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Tasks
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.storage.FirebaseStorage
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.data.util.TeamCache
import com.supercilex.robotscouter.data.util.TeamHelper
import com.supercilex.robotscouter.util.AsyncTaskExecutor
import com.supercilex.robotscouter.util.SINGLE_ITEM
import com.supercilex.robotscouter.util.TEAMS_LINK_BASE
import com.supercilex.robotscouter.util.createFile
import com.supercilex.robotscouter.util.isOffline
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class TeamSharer private constructor(private val activity: FragmentActivity,
                                     @Size(min = 1) teamHelpers: List<TeamHelper>) {
    private val cache: Cache

    private val safeMessage: String
        get() {
            val message: String

            val fullMessage = cache.shareMessage
            if (fullMessage.length >= MAX_MESSAGE_LENGTH) {
                message = activity.resources.getQuantityString(
                        R.plurals.share_message,
                        SINGLE_ITEM,
                        cache.teamHelpers[0].toString() + " and more")
            } else {
                message = fullMessage
            }

            return message
        }

    init {
        cache = Cache(teamHelpers)


        AsyncTaskExecutor.execute(object : Callable<String> {
            private val tempShareTemplateFile: File
                get() {
                    val nameSplit = SHARE_TEMPLATE_FILE_NAME.split(".")
                    return createFile(nameSplit[0], nameSplit[1], activity.cacheDir, null)
                }

            override fun call(): String {
                val shareTemplateFile = File(activity.cacheDir, SHARE_TEMPLATE_FILE_NAME)
                return if (shareTemplateFile.exists()) {
                    if (TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - shareTemplateFile.lastModified()) >= FRESHNESS) {
                        if (shareTemplateFile.delete()) {
                            getShareTemplateFromServer(tempShareTemplateFile)
                        } else {
                            throw FileSystemException(
                                    shareTemplateFile, reason = "Could not delete old file.")
                        }
                    } else {
                        shareTemplateFile.readText()
                    }
                } else {
                    getShareTemplateFromServer(tempShareTemplateFile)
                }
            }

            private fun getShareTemplateFromServer(to: File): String {
                Tasks.await(FirebaseStorage.getInstance()
                        .reference
                        .child(SHARE_TEMPLATE_FILE_NAME)
                        .getFile(to))
                return to.readText()
            }
        }).continueWith(AsyncTaskExecutor.INSTANCE, Continuation<String, Intent> {
            val deepLinkBuilder = StringBuilder("$TEAMS_LINK_BASE?")
            for (teamHelper in cache.teamHelpers) {
                deepLinkBuilder.append(teamHelper.linkKeyNumberPair)
            }

            getInvitationIntent(deepLinkBuilder.toString(), String.format(
                    it.result,
                    cache.shareCta,
                    cache.teamHelpers[0].team.media))
        }).addOnSuccessListener {
            activity.startActivityForResult(it, RC_SHARE)
        }.addOnFailureListener {
            FirebaseCrash.report(it)
            Snackbar.make(activity.findViewById<View>(R.id.root),
                    R.string.general_error,
                    Snackbar.LENGTH_LONG).show()
        }
    }

    private fun getInvitationIntent(deepLink: String, shareTemplate: String) =
            AppInviteInvitation.IntentBuilder(cache.shareTitle)
                    .setMessage(safeMessage)
                    .setDeepLink(Uri.parse(deepLink))
                    .setEmailSubject(cache.shareCta)
                    .setEmailHtmlContent(shareTemplate)
                    .build()

    private inner class Cache(teamHelpers: Collection<TeamHelper>) : TeamCache(teamHelpers) {
        val shareMessage: String
        val shareCta: String
        val shareTitle: String

        init {
            val resources = activity.resources
            val quantity = teamHelpers.size

            shareMessage = resources.getQuantityString(R.plurals.share_message, quantity, teamNames)
            shareCta = resources.getQuantityString(R.plurals.share_call_to_action, quantity, teamNames)
            shareTitle = resources.getQuantityString(R.plurals.share_title, quantity, teamNames)
        }
    }

    companion object {
        private const val RC_SHARE = 9
        private const val MAX_MESSAGE_LENGTH = 100
        private const val FRESHNESS = 7L
        private const val SHARE_TEMPLATE_FILE_NAME = "share_template.html"

        /**
         * @return true if a share intent was launched, false otherwise
         */
        fun shareTeams(activity: FragmentActivity,
                       @Size(min = 1) teamHelpers: List<TeamHelper>): Boolean {
            if (isOffline(activity)) {
                Snackbar.make(activity.findViewById<View>(R.id.root),
                        R.string.no_connection,
                        Snackbar.LENGTH_LONG)
                        .show()
                return false
            }
            if (teamHelpers.isEmpty()) return false

            TeamSharer(activity, teamHelpers)
            return true
        }
    }
}

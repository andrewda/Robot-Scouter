package com.supercilex.robotscouter.util

import com.google.firebase.database.DatabaseReference

val templateIndicesRef: DatabaseReference get() = getTemplateIndicesRef(uid!!)

fun getTemplateIndicesRef(uid: String): DatabaseReference =
        FIREBASE_USERS.child(uid).child(SCOUT_TEMPLATE_INDICES)

fun getTemplateMetricsRef(key: String): DatabaseReference =
        FIREBASE_TEMPLATES.child(key).child(FIREBASE_METRICS)

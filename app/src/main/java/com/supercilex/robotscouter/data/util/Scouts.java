package com.supercilex.robotscouter.data.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.util.Pair;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.supercilex.robotscouter.data.model.Metric;
import com.supercilex.robotscouter.data.model.Scout;
import com.supercilex.robotscouter.util.AsyncTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static com.supercilex.robotscouter.data.util.ScoutUtilsKt.getMETRIC_PARSER;
import static com.supercilex.robotscouter.data.util.ScoutUtilsKt.getScoutIndicesRef;
import static com.supercilex.robotscouter.util.ConnectivityUtilsKt.isOffline;
import static com.supercilex.robotscouter.util.ConstantsKt.FIREBASE_METRICS;
import static com.supercilex.robotscouter.util.ConstantsKt.FIREBASE_NAME;
import static com.supercilex.robotscouter.util.ConstantsKt.getFIREBASE_SCOUTS;

public final class Scouts implements OnFailureListener, OnSuccessListener<Pair<TeamHelper, List<String>>> {
    private final TaskCompletionSource<Map<TeamHelper, List<Scout>>> mScoutsTask = new TaskCompletionSource<>();
    private final Map<TeamHelper, List<Scout>> mScouts = new ConcurrentHashMap<>();
    private final List<Task<Void>> mScoutMetricsTasks = new CopyOnWriteArrayList<>();

    private final List<TeamHelper> mTeamHelpers;
    private final Context mContext;

    private Scouts(@Size(min = 1) List<TeamHelper> helpers, Context appContext) {
        mTeamHelpers = helpers;
        mContext = appContext;
    }

    public static Task<Map<TeamHelper, List<Scout>>> getAll(@Size(min = 1) List<TeamHelper> teamHelpers,
                                                            Context appContext) {
        return new Scouts(teamHelpers, appContext).build();
    }

    private Task<Map<TeamHelper, List<Scout>>> build() {
        List<Task<Pair<TeamHelper, List<String>>>> scoutIndicesTasks = new ArrayList<>();
        for (TeamHelper helper : mTeamHelpers) {
            TaskCompletionSource<Pair<TeamHelper, List<String>>> scoutIndicesTask = new TaskCompletionSource<>();
            scoutIndicesTasks.add(scoutIndicesTask.getTask());

            getScoutIndicesRef(helper.getTeam().getKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            AsyncTaskExecutor.Companion.execute(() -> {
                                List<String> scoutKeys = new ArrayList<>();
                                for (DataSnapshot scoutKeyTemplate : snapshot.getChildren()) {
                                    scoutKeys.add(scoutKeyTemplate.getKey());
                                }
                                return scoutKeys;
                            }).addOnSuccessListener(scoutKeys -> scoutIndicesTask.setResult(
                                    Pair.create(helper, scoutKeys)));
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            scoutIndicesTask.setException(error.toException());
                            FirebaseCrash.report(error.toException());
                        }
                    });
        }


        for (Task<Pair<TeamHelper, List<String>>> scoutKeysTask : scoutIndicesTasks) {
            scoutKeysTask.addOnSuccessListener(this).addOnFailureListener(this);
        }

        Tasks.whenAll(scoutIndicesTasks)
                .addOnSuccessListener(aVoid -> Tasks.whenAll(mScoutMetricsTasks)
                        .addOnSuccessListener(aVoid1 -> mScoutsTask.setResult(mScouts))
                        .addOnFailureListener(this))
                .addOnFailureListener(this);


        return mScoutsTask.getTask();
    }

    @Override
    public void onSuccess(Pair<TeamHelper, List<String>> pair) {
        if (pair.second.isEmpty()) {
            mScouts.put(pair.first, new ArrayList<>());
            return;
        }

        for (String scoutKey : pair.second) {
            TaskCompletionSource<Void> scoutMetricsTask = new TaskCompletionSource<>();
            mScoutMetricsTasks.add(scoutMetricsTask.getTask());

            new ScoutListener(getFIREBASE_SCOUTS().child(scoutKey), pair, scoutMetricsTask);
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        mScoutsTask.setException(e);
    }

    private class ScoutListener implements ChildEventListener, ValueEventListener {
        private static final int TIMEOUT = 1;

        private final Query mQuery;
        private final DatabaseReference mMetricsQuery;
        private final Pair<TeamHelper, List<String>> mPair;
        private final TaskCompletionSource<Void> mScoutMetricsTask;

        private String mName;
        private final List<Metric<?>> mMetrics = new ArrayList<>();

        private Timer mTimer = new Timer();

        public ScoutListener(Query query,
                             Pair<TeamHelper, List<String>> pair,
                             TaskCompletionSource<Void> scoutMetricsTask) {
            mQuery = query;
            mMetricsQuery = mQuery.getRef().child(FIREBASE_METRICS);
            mPair = pair;
            mScoutMetricsTask = scoutMetricsTask;

            resetTimeout();
            addListeners();
        }

        private void addListeners() {
            mMetricsQuery.addChildEventListener(this);
            mQuery.addValueEventListener(this);
        }

        @Override
        public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
            mMetrics.add(getMETRIC_PARSER().parseSnapshot(snapshot));
            resetTimeout();
        }

        private void resetTimeout() {
            if (isOffline(mContext)) {
                mTimer.cancel();
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, TimeUnit.SECONDS.toMillis(TIMEOUT));
            }
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            // This is the hackiest thing ever! See https://github.com/firebase/FirebaseUI-Android/pull/477#issuecomment-270283875
            // for why this hack is necessary.

            mName = snapshot.child(FIREBASE_NAME).getValue(String.class);
            finish();
        }

        private void finish() {
            mTimer.cancel();
            mMetricsQuery.removeEventListener((ChildEventListener) this);
            mQuery.removeEventListener((ValueEventListener) this);

            Scout scout = new Scout(mName, mMetrics);
            if (!scout.getMetrics().isEmpty()) {
                List<Scout> scouts = mScouts.get(mPair.first);
                if (scouts == null) {
                    List<Scout> scoutList = new ArrayList<>();
                    scoutList.add(scout);
                    mScouts.put(mPair.first, scoutList);
                } else {
                    scouts.add(scout);
                    mScouts.put(mPair.first, scouts);
                }
            }

            mScoutMetricsTask.trySetResult(null);
        }

        @Override
        public void onCancelled(DatabaseError error) {
            mScoutMetricsTask.setException(error.toException());
            FirebaseCrash.report(error.toException());
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
            // Noop
        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
            // Noop
        }

        @Override
        public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
            // Noop
        }
    }
}

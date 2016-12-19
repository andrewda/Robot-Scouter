package com.supercilex.robotscouter.data.model;

import android.os.Bundle;
import android.support.annotation.Keep;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.supercilex.robotscouter.util.BaseHelper;
import com.supercilex.robotscouter.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class Scout {
    private String mOwner;
    private Map<String, ScoutMetric> mScoutMetrics = new HashMap<>();

    @Exclude
    public static DatabaseReference getIndicesRef() {
        return Constants.FIREBASE_SCOUT_INDICES.child(BaseHelper.getUid());
    }

    public static void add(final Team team) {
        final DatabaseReference indexRef = getIndicesRef().push();
        DatabaseReference scoutRef = Constants.FIREBASE_SCOUTS.child(indexRef.getKey());

        ScoutCopier scoutCopier = new ScoutCopier(scoutRef) {
            @Override
            protected void onDone() {
                indexRef.setValue(Long.parseLong(team.getNumber()));
            }
        };
        if (team.getTemplateKey() == null) {
            Constants.FIREBASE_DEFAULT_TEMPLATE.addListenerForSingleValueEvent(scoutCopier);
        } else {
            Constants.FIREBASE_SCOUT_TEMPLATES
                    .child(team.getTemplateKey())
                    .addListenerForSingleValueEvent(scoutCopier);
        }
//        mScoutMetrics = new LinkedHashMap<>();
//
//        addView(scoutRef, new ScoutMetric<>("example yes or no value pos 1",
//                                          false).setType((Integer) Constants.CHECKBOX));
//        addView(scoutRef, new ScoutMetric<>("test pos 2", true).setType((Integer) Constants.CHECKBOX));
//        addView(scoutRef, new ScoutMetric<>("auto scores pos 3", 0).setType((Integer) Constants.COUNTER));
//        addView(scoutRef, new ScoutMetric<>("teleop scores pos 4", 0).setType((Integer) Constants.COUNTER));
//        ArrayList<String> list = new ArrayList<>();
//        list.add("test");
//        list.add("test 2");
//        list.add("test 3");
//        list.add("test 4");
//        addView(scoutRef, new ScoutSpinner("some name", list, 0));
//        addView(scoutRef, new ScoutSpinner("foobar!", list, 0));
//        addView(scoutRef,
//                new ScoutMetric<>("note 1 pos 5", "some note").setType((Integer) Constants.EDIT_TEXT));
//        addView(scoutRef,
//                new ScoutMetric<>("note 2 pos 6", "some other note").setType((Integer) Constants.EDIT_TEXT));
    }

    public static Bundle getScoutKeyBundle(String key) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SCOUT_KEY, key);
        return bundle;
    }

    public static String getScoutKey(Bundle bundle) {
        return bundle.getString(Constants.SCOUT_KEY);
    }

    @Keep
    public String getOwner() {
        return mOwner;
    }

    @Keep
    public Map<String, ScoutMetric> getViews() {
        return mScoutMetrics;
    }

    public void addView(String key, ScoutMetric view) {
        mScoutMetrics.put(key, view);
    }
}

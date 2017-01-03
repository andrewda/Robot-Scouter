package com.supercilex.robotscouter.ui.teamlist;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.supercilex.robotscouter.data.model.Team;
import com.supercilex.robotscouter.ui.scout.ScoutActivity;

import java.util.ArrayList;
import java.util.List;

public final class TeamReceiver implements ResultCallback<AppInviteInvitationResult> {
    public static final String TEAM_QUERY_KEY = "team";
    private static final String UTM_SOURCE = "utm_source";
    private static final String UTM_SOURCE_VALUE = "robotscouter";
    public static final String APP_LINK_BASE = "https://supercilex.github.io/?" + TeamReceiver.UTM_SOURCE + "=" + TeamReceiver.UTM_SOURCE_VALUE;

    private FragmentActivity mActivity;

    private TeamReceiver(FragmentActivity activity) {
        mActivity = activity;

        // Check for deep links
        AppInvite.AppInviteApi
                .getInvitation(new GoogleApiClient.Builder(mActivity)
                                       .enableAutoManage(mActivity, null)
                                       .addApi(AppInvite.API)
                                       .build(),
                               null,
                               false)
                .setResultCallback(this);
    }

    public static TeamReceiver init(FragmentActivity activity) {
        return new TeamReceiver(activity);
    }

    @Override
    public void onResult(@NonNull AppInviteInvitationResult result) {
        Uri deepLink = mActivity.getIntent().getData();
        if (deepLink == null
                || !deepLink.getQueryParameter(UTM_SOURCE).equals(UTM_SOURCE_VALUE)
                || deepLink.getQueryParameter(TEAM_QUERY_KEY) == null) {
            return; // Nothing to see here
        }

        // Received invite from Firebase dynamic links
        if (result.getStatus().isSuccess()) {
            List<Team> teams = getTeam(Uri.parse(AppInviteReferral.getDeepLink(result.getInvitationIntent())));
            for (Team team : teams) {
                Long number = team.getNumberAsLong();
                Team.getIndicesRef().child(team.getKey()).setValue(number, number);
            }
            if (teams.size() == 1) launchTeam(teams.get(0));
        } else { // Received normal one team intent
            launchTeam(getTeam(deepLink).get(0));
        }
    }

    private List<Team> getTeam(Uri deepLink) {
        List<Team> teams = new ArrayList<>();
        for (String teamPair : deepLink.getQueryParameters(TEAM_QUERY_KEY)) {
            // Format: -key:2521
            String[] teamPairSplit = teamPair.split(":");
            String teamKey = teamPairSplit[0];
            String teamNumber = teamPairSplit[1];

            teams.add(new Team.Builder(teamNumber).setKey(teamKey).build());
        }
        return teams;
    }

    private void launchTeam(Team team) {
        ScoutActivity.start(mActivity, team, false);

        // Consume intent
        mActivity.setIntent(new Intent());
    }
}
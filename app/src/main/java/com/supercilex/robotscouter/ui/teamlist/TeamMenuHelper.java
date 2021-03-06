package com.supercilex.robotscouter.ui.teamlist;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.supercilex.robotscouter.R;
import com.supercilex.robotscouter.data.client.spreadsheet.ExportService;
import com.supercilex.robotscouter.data.model.Team;
import com.supercilex.robotscouter.data.util.TeamHelper;
import com.supercilex.robotscouter.ui.PermissionRequestHandler;
import com.supercilex.robotscouter.ui.TeamDetailsDialog;
import com.supercilex.robotscouter.ui.TeamSharer;

import java.util.ArrayList;
import java.util.List;

import static com.supercilex.robotscouter.util.AnalyticsUtilsKt.logEditTeamDetailsEvent;
import static com.supercilex.robotscouter.util.AnalyticsUtilsKt.logShareTeamEvent;
import static com.supercilex.robotscouter.util.AuthUtilsKt.isFullUser;
import static com.supercilex.robotscouter.util.ConstantsKt.SINGLE_ITEM;
import static com.supercilex.robotscouter.util.FirebaseAdapterUtilsKt.notifyAllItemsChangedNoAnimation;
import static com.supercilex.robotscouter.util.IoUtilsKt.getIO_PERMS;
import static com.supercilex.robotscouter.util.ViewUtilsKt.animateColorChange;

public class TeamMenuHelper implements TeamMenuManager, OnSuccessListener<Void>, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String SELECTED_TEAMS_KEY = "selected_teams_key";

    private final Fragment mFragment;
    private final PermissionRequestHandler mPermHandler;

    private final List<TeamHelper> mSelectedTeams = new ArrayList<>();

    /**
     * Do not use.
     * <p>
     * When TeamMenuHelper is initialized, {@link View#findViewById(int)} returns null because
     * setContentView has not yet been called in the Fragment's activity.
     *
     * @see #getFab()
     */
    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<Team, TeamViewHolder> mAdapter;


    private boolean mIsMenuReady;

    private MenuItem mExportItem;
    private MenuItem mShareItem;
    private MenuItem mVisitTbaWebsiteItem;
    private MenuItem mVisitTeamWebsiteItem;
    private MenuItem mEditTeamDetailsItem;
    private MenuItem mDeleteItem;

    private MenuItem mSignInItem;
    private MenuItem mSignOutItem;
    private MenuItem mDonateItem;
    private MenuItem mLicencesItem;
    private MenuItem mAboutItem;

    public TeamMenuHelper(Fragment fragment) {
        mFragment = fragment;
        mPermHandler = new PermissionRequestHandler(getIO_PERMS(), mFragment, this);
    }

    public void setAdapter(FirebaseRecyclerAdapter<Team, TeamViewHolder> adapter) {
        mAdapter = adapter;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    public boolean noItemsSelected() {
        return mSelectedTeams.isEmpty();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mIsMenuReady = true;
        inflater.inflate(R.menu.team_options, menu);

        mExportItem = menu.findItem(R.id.action_export_spreadsheet);
        mShareItem = menu.findItem(R.id.action_share);
        mVisitTbaWebsiteItem = menu.findItem(R.id.action_visit_tba_website);
        mVisitTeamWebsiteItem = menu.findItem(R.id.action_visit_team_website);
        mEditTeamDetailsItem = menu.findItem(R.id.action_edit_team_details);
        mDeleteItem = menu.findItem(R.id.action_delete);

        mSignInItem = menu.findItem(R.id.action_sign_in);
        mSignOutItem = menu.findItem(R.id.action_sign_out);
        mDonateItem = menu.findItem(R.id.action_donate);
        mLicencesItem = menu.findItem(R.id.action_licenses);
        mAboutItem = menu.findItem(R.id.action_about);

        updateState();
    }

    private void updateState() {
        if (!mSelectedTeams.isEmpty()) {
            setNormalMenuItemsVisible(false);
            setContextMenuItemsVisible(true);
            if (mSelectedTeams.size() == SINGLE_ITEM) {
                setTeamSpecificItemsVisible(true);
            }
            setToolbarTitle();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TeamHelper teamHelper = mSelectedTeams.get(0);
        switch (item.getItemId()) {
            case R.id.action_export_spreadsheet:
                exportTeams();
                break;
            case R.id.action_share:
                if (TeamSharer.Companion.shareTeams(mFragment.getActivity(), mSelectedTeams)) {
                    resetMenu();
                }
                logShareTeamEvent(teamHelper.getTeam().getNumber());
                break;
            case R.id.action_visit_tba_website:
                teamHelper.visitTbaWebsite(mFragment.getContext());
                resetMenu();
                break;
            case R.id.action_visit_team_website:
                teamHelper.visitTeamWebsite(mFragment.getContext());
                resetMenu();
                break;
            case R.id.action_edit_team_details:
                TeamDetailsDialog.show(mFragment.getChildFragmentManager(), teamHelper);
                logEditTeamDetailsEvent(teamHelper.getTeam().getNumber());
                break;
            case R.id.action_delete:
                DeleteTeamDialog.Companion.show(mFragment.getChildFragmentManager(), mSelectedTeams);
                break;
            case android.R.id.home:
                resetMenu();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean onBackPressed() {
        if (mSelectedTeams.isEmpty()) {
            return false;
        } else {
            resetMenu();
            return true;
        }
    }

    @Override
    public void resetMenu() {
        setContextMenuItemsVisible(false);
        setNormalMenuItemsVisible(true);
        mSelectedTeams.clear();
        notifyItemsChanged();
    }

    @Override
    public void saveState(Bundle outState) {
        outState.putParcelableArray(SELECTED_TEAMS_KEY,
                                    mSelectedTeams.toArray(new TeamHelper[mSelectedTeams.size()]));
    }

    @Override
    public void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null && mSelectedTeams.isEmpty()) {
            Parcelable[] parcelables = savedInstanceState.getParcelableArray(SELECTED_TEAMS_KEY);
            for (Parcelable parcelable : parcelables) {
                mSelectedTeams.add((TeamHelper) parcelable);
            }
            notifyItemsChanged();
        }

        if (mIsMenuReady) updateState();
    }

    @Override
    public void onTeamContextMenuRequested(TeamHelper teamHelper) {
        boolean hadNormalMenu = mSelectedTeams.isEmpty();

        int oldSize = mSelectedTeams.size();
        if (mSelectedTeams.contains(teamHelper)) { // Team already selected
            mSelectedTeams.remove(teamHelper);
        } else {
            mSelectedTeams.add(teamHelper);
        }

        setToolbarTitle();

        int newSize = mSelectedTeams.size();
        if (hadNormalMenu) {
            setNormalMenuItemsVisible(false);
            setContextMenuItemsVisible(true);
            setTeamSpecificItemsVisible(true);
            notifyItemsChanged();
        } else {
            if (mSelectedTeams.isEmpty()) {
                resetMenu();
            } else if (newSize == SINGLE_ITEM) {
                setTeamSpecificItemsVisible(true);
            } else {
                setTeamSpecificItemsVisible(false);
            }

            if (newSize > oldSize && newSize > SINGLE_ITEM && mAdapter.getItemCount() > newSize) {
                Snackbar.make(mFragment.getView(),
                              R.string.multiple_teams_selected,
                              Snackbar.LENGTH_LONG)
                        .setAction(R.string.select_all, v -> {
                            mSelectedTeams.clear();
                            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                                mSelectedTeams.add(mAdapter.getItem(i).getHelper());
                            }
                            updateState();
                            notifyItemsChanged();
                        })
                        .show();
            }
        }
    }

    @Override
    public List<TeamHelper> getSelectedTeams() {
        return mSelectedTeams;
    }

    @Override
    public void onSelectedTeamChanged(TeamHelper oldTeamHelper, TeamHelper teamHelper) {
        mSelectedTeams.remove(oldTeamHelper);
        mSelectedTeams.add(teamHelper);
    }

    @Override
    public void onSelectedTeamRemoved(TeamHelper oldTeamHelper) {
        mSelectedTeams.remove(oldTeamHelper);
        if (mSelectedTeams.isEmpty()) {
            resetMenu();
        } else {
            setToolbarTitle();
        }
    }

    private void setContextMenuItemsVisible(boolean visible) {
        mExportItem.setVisible(visible);
        mShareItem.setVisible(visible);
        mDeleteItem.setVisible(visible);
        ((AppCompatActivity) mFragment.getActivity()).getSupportActionBar()
                .setDisplayHomeAsUpEnabled(visible);
        if (visible) getFab().hide();
    }

    private void setTeamSpecificItemsVisible(boolean visible) {
        mVisitTbaWebsiteItem.setVisible(visible);
        mVisitTeamWebsiteItem.setVisible(visible);
        mEditTeamDetailsItem.setVisible(visible);

        if (visible) {
            Team team = mSelectedTeams.get(0).getTeam();

            mVisitTbaWebsiteItem.setTitle(
                    mFragment.getString(R.string.visit_team_website_on_tba, team.getNumber()));
            mVisitTeamWebsiteItem.setTitle(
                    mFragment.getString(R.string.visit_team_website, team.getNumber()));
        }
    }

    private void setNormalMenuItemsVisible(boolean visible) {
        mDonateItem.setVisible(visible);
        mLicencesItem.setVisible(visible);
        mAboutItem.setVisible(visible);

        if (visible) {
            getFab().show();
            ((AppCompatActivity) mFragment.getActivity()).getSupportActionBar()
                    .setTitle(R.string.app_name);

            if (isFullUser()) {
                mSignInItem.setVisible(false);
                mSignOutItem.setVisible(true);
            } else {
                mSignInItem.setVisible(true);
                mSignOutItem.setVisible(false);
            }
            setTeamSpecificItemsVisible(false);
        } else {
            mSignInItem.setVisible(false);
            mSignOutItem.setVisible(false);
        }

        updateToolbarColor(visible);
    }

    private void updateToolbarColor(boolean visible) {
        FragmentActivity activity = mFragment.getActivity();

        @ColorRes int oldColorPrimary = visible ? R.color.selected_toolbar : R.color.colorPrimary;
        @ColorRes int newColorPrimary = visible ? R.color.colorPrimary : R.color.selected_toolbar;

        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        animateColorChange(
                mFragment.getContext(),
                oldColorPrimary,
                newColorPrimary,
                animator -> toolbar.setBackgroundColor((int) animator.getAnimatedValue()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @ColorRes int oldColorPrimaryDark = visible ? R.color.selected_status_bar : R.color.colorPrimaryDark;
            @ColorRes int newColorPrimaryDark = visible ? R.color.colorPrimaryDark : R.color.selected_status_bar;

            animateColorChange(
                    mFragment.getContext(),
                    oldColorPrimaryDark,
                    newColorPrimaryDark,
                    animator -> activity.getWindow()
                            .setStatusBarColor((int) animator.getAnimatedValue()));
        }
    }

    private void setToolbarTitle() {
        ((AppCompatActivity) mFragment.getActivity()).getSupportActionBar()
                .setTitle(String.valueOf(mSelectedTeams.size()));
    }

    private void notifyItemsChanged() {
        notifyAllItemsChangedNoAnimation(mRecyclerView, mAdapter);
    }

    private FloatingActionButton getFab() {
        if (mFab == null) {
            mFab = mFragment.getActivity().findViewById(R.id.fab);
        }
        return mFab;
    }

    @Override
    public void onSuccess(Void aVoid) {
        exportTeams();
    }

    private void exportTeams() {
        if (ExportService.exportAndShareSpreadSheet(
                mFragment, mPermHandler, mSelectedTeams)) {
            resetMenu();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mPermHandler.onRequestPermissionsResult(requestCode,
                                                permissions,
                                                grantResults);
    }

    public void onActivityResult(int requestCode) {
        mPermHandler.onActivityResult(requestCode);
    }
}

package com.supercilex.robotscouter.data.job;

//import com.firebase.jobdispatcher.JobParameters;
//import com.firebase.jobdispatcher.JobService;
//import com.supercilex.robotscouter.util.Constants;
//import com.supercilex.robotscouter.model.team.Team;
//
//public class DownloadTeamDataJob extends JobService {
//    @Override
//    public boolean onStartJob(final JobParameters params) {
//        // Note: this is preformed on the main thread.
//
//        final String number = params.getExtras().getString(Constants.INTENT_TEAM_NUMBER);
//        final String key = params.getExtras().getString(Constants.INTENT_TEAM_KEY);
//
//        new Thread(new Runnable() {
//            public void run() {
//                new TbaService(number, new Team(), DownloadTeamDataJob.this) {
//                    @Override
//                    public void onFinished(Team team, boolean isSuccess) {
//                        if (isSuccess) {
//                            team.update(number, key);
//                            jobFinished(params, false);
//                        } else {
//                            jobFinished(params, true);
//                        }
//                    }
//                };
//            }
//        }).start();
//
//        return true;
//    }
//
//    @Override
//    public boolean onStopJob(JobParameters params) {
//        // Note: return true to reschedule this job.
//        return true;
//    }
//}

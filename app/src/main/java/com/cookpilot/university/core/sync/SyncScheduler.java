package com.cookpilot.university.core.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class SyncScheduler {

    private static final String IMMEDIATE_WORK = "cookpilot_user_sync";
    private static final String PERIODIC_WORK = "cookpilot_user_sync_periodic";

    private SyncScheduler() {
    }

    public static void requestSync(@NonNull Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(UserSyncWorker.class)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(
                        IMMEDIATE_WORK,
                        ExistingWorkPolicy.REPLACE,
                        request
                );
    }

    public static void ensurePeriodicSync(@NonNull Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        UserSyncWorker.class,
                        15,
                        TimeUnit.MINUTES
                )
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(
                        PERIODIC_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        request
                );
    }
}

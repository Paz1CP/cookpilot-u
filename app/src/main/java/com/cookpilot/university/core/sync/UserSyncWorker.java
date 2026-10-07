package com.cookpilot.university.core.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.cookpilot.university.CookPilotApplication;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public final class UserSyncWorker extends Worker {

    public UserSyncWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams
    ) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        CookPilotApplication application =
                (CookPilotApplication) getApplicationContext();

        if (!application.getAuthRepository()
                .getAuthState()
                .isAuthenticated()) {
            return Result.success();
        }

        LocalDate weekStart = LocalDate.now().with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        try {
            application.getCookPlanRepository().syncPendingBlocking();
            application.getShoppingRepository().syncPendingBlocking();
            application.getCookPlanRepository().refreshWeekBlocking(weekStart);
            application.getShoppingRepository().refreshWeekBlocking(weekStart);
            return Result.success();
        } catch (Exception exception) {
            return Result.retry();
        }
    }
}

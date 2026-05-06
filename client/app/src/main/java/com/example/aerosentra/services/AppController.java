package com.example.aerosentra.services;

import android.app.Application;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class AppController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleWorker();
    }

    private void scheduleWorker() {
        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(
                    DataExpiryWorker.class,
                    30,
                    TimeUnit.MINUTES
                ).build();
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        "data_expiry_worker",
                        ExistingPeriodicWorkPolicy.KEEP,
                        workRequest
                );
    }
}

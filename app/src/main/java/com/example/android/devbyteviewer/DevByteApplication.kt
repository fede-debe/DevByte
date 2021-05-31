/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.devbyteviewer

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.android.devbyteviewer.work.RefreshDataWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Override application to setup background work via WorkManager. This is the Android application, which is the main object that the operating system uses to interact with the app.
 * It lives underneath every activity and screen in the app. Application onCreate() is a really important method, it runs every time the app launches and it has to run before any
 * screen is shown.
 */
class DevByteApplication : Application() {

    /**
     * To make sure that onCreate is fast, we declare this extra initialization out of it. avoid expensive task on the main thread */
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    /**
     * onCreate is called before the first screen is shown to the user.
     *
     * Use it to setup any background tasks, running expensive setup operations in a background
     * thread to avoid delaying app start.
     *
     * Timber.plant call to configure logging in this app
     */
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        /** Run the initialization in the background instead of doing it before the first screen shows */
        delayedInit()
    }

    /** we use coroutine by calling launch to do it on a background thread. */
    private fun delayedInit() = applicationScope.launch {
        setupRecurringWork()
    }

    /** function that handles scheduling work. WorkManagers schedules work with a "work request" and there are 2 types of it: 1) one time, 2) periodic. We want to run this
     * task daily so we use a periodicRequest. We set to make a periodic work request for our worker, in the constructor we tell it how often to schedule this work and we
     * need to build it. To schedule the work we use an enqueue method on WorkManager that's called UniquePeriodicWork, this work is scheduled to execute regularly or on a
     * periodic basis. It's also unique, that's why we need to pass a work_name that is unique for this work(WorkManager treat requests with the same name as same job).
     * An ExistingPeriodicWorkPolicy tells WorkManager what to do when 2 requests for the same unique work are enqueued. Inside this app we can keep the previous periodic work
     * which will discard the new work request (the other option is REPLACE) */
    private fun setupRecurringWork() {
        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWork>(
            1,
            TimeUnit.DAYS
        ).build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            RefreshDataWork.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest)
    }
}

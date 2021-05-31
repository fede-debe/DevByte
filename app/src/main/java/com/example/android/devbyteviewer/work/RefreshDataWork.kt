package com.example.android.devbyteviewer.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android.devbyteviewer.database.getDatabase
import com.example.android.devbyteviewer.repository.VideosRepository
import retrofit2.HttpException

/**
 * We are creating a worker that fetches new videos from the network by using the existing repository. This Worker extends CoroutineWorker. WorkManager supports Coroutines
 * directly if we use CoroutineWorker. Worker classes have to implement doWork() suspend method. The worker will run until do work returns the result ,we can do different things
 * like access the network right to the database and WorkManager will make sure the operating system doesn't interrupt the work. doWork() will run in the background thread and we
 * don't have to worry about blocking the UI thread. Inside doWork() we get a database and a repository (just like the ViewModel). To let know the WorkManager know we are done,
 * we need to return result.SUCCESS . In the case that the network fails or the backend server is off, we need to implement a try/catch block for the network requests that
 * will fail. Whenever there's a HTTP exception from retrofit to retry this job sometime in the future. The worker of WorkManager is done(unit of work, extend worker,
 * implement doWork()! if you call Coroutine, you can use the Coroutine workers superclass to use your Coroutine from a worker.
 * */

class RefreshDataWork(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params) {

    /** constant used to uniquely identify this worker */
    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }
    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = VideosRepository(database)
        return try {
            repository.refreshVideos()
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }
}
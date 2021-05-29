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

package com.example.android.devbyteviewer.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.devbyteviewer.database.VideosDatabase
import com.example.android.devbyteviewer.database.asDomainModel
import com.example.android.devbyteviewer.domain.Video
import com.example.android.devbyteviewer.network.Network
import com.example.android.devbyteviewer.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository are just regular classes(no extensions/no annotations), they are responsible for providing a simple API to our data sources. We'll need a videoDatabase for the
 * repository and we'll ask users to pass it as a constructor parameter(simple form of concept called DEPENDENCY INJECTION. By taking a database object as a constructor parameter,
 * we don't need to keep a reference to Android context in the repository, potentially causing leaks. Here I will split the API into 2 parts: one to load videos from the offline
 * cache and another to refresh the offline cache.
 */

class VideosRepository(private val database: VideosDatabase) {

    /**
     * Main interface for the Repository. This is the property that everyone can use to observe videos from the repository. I need to run the database Query "getVideos()" and it
     * give me a LiveData with a database video and we only need a video. We use Transformation.map(let us convert from a LiveData to another) to map from database video to video.
     * I can use the helper ".asDomainModel" to convert a database video to a video. The Transformation will only happen when an activity or fragment is listening, so we can
     * declare this property safely*/
    val videos: LiveData<List<Video>> =  Transformations.map(database.videoDao.getVideos()) {
        it.asDomainModel()
    }

    /**
     * It doesn't return anything, it's not accidentally used as the API to fetch videos, it's just responsible for updating the offline cache. It is a
     * suspend method because I will call it from a coroutine. We are going to make a Database call to save the new videos to the database. Inside the IO thread we need to make a
     * network call, we ask the network to get the playlist and use the await function to tell the coroutine to suspend until it's available. As this executes, you can be sure that the
     * result of getPlaylist() is always available, but it doesn't block this thread while waiting for it. This is because await is a suspended function. Then we make the database
     * call by using insertAll on videoDao. To keep the separation of concerns, we'll need to map  the  network results to database objects using ".asDatabaseModel".
     */
    // refresh the offline cache
    suspend fun refreshVideos() {
        withContext(Dispatchers.IO) {
            val playlist = Network.devbytes.getPlaylist().await()
            database.videoDao.insertAll(*playlist.asDatabaseModel())
        }
    }
}
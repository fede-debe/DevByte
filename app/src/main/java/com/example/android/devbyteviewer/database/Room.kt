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

package com.example.android.devbyteviewer.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

/**
    DAO for the offline cache. It needs 2 methods: one to load values from the cache, another to store values.
    When a new network result comes in, it'll get passed to insertAll() to save to the database. The UI will load values using getVideos().
 */
@Dao
interface VideoDao {

    /**
        We get all videos from the cache, that's why we return a list of <DatabaseVideo>. We need to return a list of LiveData, this is because
        a UI will watch or observe this query, we have to tell the UI that the database changed. When we query room from the UI thread and it returns
        a basic type like list, it will block the UI thread. When we return a LiveData, Room will do the database query in the background for us and it
        will update  the LiveData  anytime new data is written to the table.
    */
    @Query("select * from databasevideo")
    fun getVideos(): LiveData<List<DatabaseVideo>>

    /**
        Store values into the cache, as argument we pass a vararg  of DatabaseVideo (variable/argument - it's how a function can take an unknown number of arguments in Kotlin.
        It'll pass an array under the hood. In this way they can pass a few videos without making a list). Since we want to override the last saved value with the new one, set
        the conflictStrategy to REPLACE as an onConflict argument.
    */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg videos: DatabaseVideo)

}

/**
 * After implementing the DAO, we only need to implement a database and getDatabase. Since we only have one DAO, the database class will be simple. We declare the abstract val
 * videoDao to get access to the DAO. We declare it as database with the entity it holds + the version of the database.
 */

@Database(entities = [DatabaseVideo::class], version = 1)
abstract class VideosDatabase: RoomDatabase() {
    abstract val videoDao: VideoDao
}

/**
 * There's a couple of way to declare getDatabase, we are going to use a singleton (an object that can have only one instance) here. The variable that holds the singleton is declared
 * private because we don't want to access the instance directly. The public way to access the database will be through getDatabase, it'll return the INSTANCE variable after it gets
 * initialized. To check if it's actually initialize, we can use the Kotlin function ".isInitialized" that is available to lateinit vars to check if they've already been assigned
 * to something. By adding "!::" we are saying " if INSTANCE is NOT initialized", and if it's not we can use Room.databaseBuilder to create a new instance as usual. We can also make]
 * the initialization thread-safe by wrapping it in synchronized.
 */

private lateinit var INSTANCE: VideosDatabase

fun getDatabase(context: Context): VideosDatabase {
    synchronized(VideosDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
            VideosDatabase::class.java,
            "videos").build()
        }
    }
    return INSTANCE
}


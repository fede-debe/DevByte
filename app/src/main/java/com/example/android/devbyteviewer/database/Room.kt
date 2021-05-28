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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// DAO for the offline cache. It needs 2 methods: one to load values from the cache, another to store values.
// When a new network result comes in, it'll get passed to insertAll() to save to the database. The UI will load values using getVideos().
@Dao
interface VideoDao {

    // we get all videos from the cache, that's why we return a list of <DatabaseVideo>
    @Query("select * from databasevideo")
    fun getVideos(): List<DatabaseVideo>

    // store values into the cache, as argument we pass a vararg  of DatabaseVideo (variable/argument - it's how a function can take an unknown number of arguments in Kotlin.
    // It'll pass an array under the hood. In this way they can pass a few videos without making a list). Since we want to override the last saved value with the new one, set
    // the conflictStrategy to REPLACE as an onConflict argument.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg video: DatabaseVideo)

}
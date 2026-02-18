package com.animeplayer.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlaybackPositionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePosition(playbackPosition: PlaybackPosition)

    @Query("SELECT * FROM playback_positions WHERE uri = :uri")
    suspend fun getPosition(uri: String): PlaybackPosition?
}

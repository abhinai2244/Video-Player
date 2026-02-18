package com.animeplayer.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlaybackPosition::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playbackPositionDao(): PlaybackPositionDao
}

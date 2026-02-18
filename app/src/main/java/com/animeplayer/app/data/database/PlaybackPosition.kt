package com.animeplayer.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_positions")
data class PlaybackPosition(
    @PrimaryKey val uri: String,
    val position: Long
)

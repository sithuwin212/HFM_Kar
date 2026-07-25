package com.hfm.tv.data

import androidx.room.Entity
import androidx.room.PrimaryKey

import java.io.Serializable

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,          // hash of file path
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0,
    val filePath: String,                // SAF URI or absolute path
    val source: String = "local",        // local, usb, youtube
    val addedAt: Long = System.currentTimeMillis(),
    val playCount: Int = 0
) : Serializable

@Entity(tableName = "queue")
data class QueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: String,
    val position: Int,
    val addedByPhone: String = "",
    val status: String = "pending"
)

@Entity(tableName = "paired_phones")
data class PairedPhoneEntity(
    @PrimaryKey val phoneId: String,
    val name: String = "",
    val lastConnectedAt: Long = System.currentTimeMillis(),
    val ipAddress: String = ""
)
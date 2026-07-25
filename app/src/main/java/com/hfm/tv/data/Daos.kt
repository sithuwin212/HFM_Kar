package com.hfm.tv.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY addedAt DESC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("SELECT DISTINCT artist FROM songs ORDER BY artist")
    fun getAllArtists(): Flow<List<String>>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY title")
    fun getSongsByArtist(artist: String): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    @Query("SELECT COUNT(*) FROM songs")
    fun getSongCount(): Flow<Int>

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: String)
}

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue WHERE status = 'pending' ORDER BY position")
    fun getQueue(): Flow<List<QueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToQueue(item: QueueEntity)

    @Query("DELETE FROM queue WHERE id = :id")
    suspend fun removeFromQueue(id: Long)

    @Query("UPDATE queue SET status = 'done' WHERE id = :id")
    suspend fun markDone(id: Long)

    @Query("SELECT MAX(position) FROM queue")
    suspend fun getLastPosition(): Int?

    @Query("DELETE FROM queue")
    suspend fun clearQueue()
}

@Dao
interface PairedPhoneDao {
    @Query("SELECT * FROM paired_phones ORDER BY lastConnectedAt DESC")
    fun getAllPhones(): Flow<List<PairedPhoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhone(phone: PairedPhoneEntity)

    @Query("DELETE FROM paired_phones WHERE phoneId = :phoneId")
    suspend fun removePhone(phoneId: String)

    @Query("UPDATE paired_phones SET lastConnectedAt = :time WHERE phoneId = :phoneId")
    suspend fun updateLastSeen(phoneId: String, time: Long = System.currentTimeMillis())
}
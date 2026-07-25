package com.hfm.tv.storage

import android.media.MediaMetadataRetriever
import java.io.File

class MetadataParser {

    data class SongMetadata(val title: String, val artist: String, val album: String = "")

    // Parse from filename: "Artist - Title.mp4" or "Title - Artist.mp4"
    fun parseFromFilename(fileName: String): SongMetadata {
        val name = fileName.substringBeforeLast('.')
               .trim()

           // Pattern: "Artist - Title" (most common)
        val dashPattern = Regex("^(.+?)\\s*[-–—]\\s*(.+)$")
        val match = dashPattern.find(name)

        if (match != null) {
            val part1 = match.groupValues[1].trim()
            val part2 = match.groupValues[2].trim()

            // Try to guess which is artist vs title
            // If first part is short (likely an artist name like "Sai Sai" has spaces)
            // Or if second part has [YouTubeID] suffix
            return if (part2.contains(Regex("\\[[a-zA-Z0-9_-]{11}\\]$"))) {
                // Second part has YouTube ID -> first is artist, second is title
                SongMetadata(part2, part1)
            } else {
                // Assume "Artist - Title"
                SongMetadata(part2, part1)
            }
        }

        // No dash pattern -> use filename as title, "Unknown" as artist
        return SongMetadata(
            title = name.replace(Regex("[._]"), " ").trim(),
            artist = "Unknown"
        )
    }

    // Extract metadata from media file using MediaMetadataRetriever
    fun extractFromFile(filePath: String): SongMetadata? {
        return try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(filePath)

            val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
            val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""

            mmr.release()

            if (title.isNotEmpty() && artist.isNotEmpty()) {
                SongMetadata(title, artist, album)
            } else {
                null  // Fall back to filename parsing
            }
        } catch (e: Exception) {
            null
        }
    }

    // Combined: try metadata first, fall back to filename
    fun extract(filePath: String, fileName: String): SongMetadata {
        return extractFromFile(filePath) ?: parseFromFilename(fileName)
    }
}
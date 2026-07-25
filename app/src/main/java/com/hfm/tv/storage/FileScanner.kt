package com.hfm.tv.storage

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.hfm.tv.data.SongEntity
import java.io.File
import java.security.MessageDigest

class FileScanner(private val context: Context) {

    data class ScanResult(
        val songs: List<SongEntity>,
        val errors: List<String> = emptyList()
    )

    // Scan from SAF-picked folder (Android 5+, preferred for API 30+)
    fun scanSafFolder(treeUri: Uri): ScanResult {
        val songs = mutableListOf<SongEntity>()
        val errors = mutableListOf<String>()

        try {
            val documentFile = DocumentFile.fromTreeUri(context, treeUri)
            if (documentFile == null || !documentFile.exists()) {
                return ScanResult(emptyList(), listOf("Folder not accessible"))
            }
            scanDocumentFile(documentFile, songs, errors)
        } catch (e: Exception) {
            errors.add("Scan error: ${e.message}")
        }

        return ScanResult(songs, errors)
    }

    // Scan from absolute path (for older Android versions)
    fun scanDirectory(path: String): ScanResult {
        val songs = mutableListOf<SongEntity>()
        val errors = mutableListOf<String>()
        val dir = File(path)

        if (!dir.exists() || !dir.isDirectory) {
            return ScanResult(emptyList(), listOf("Directory not found: $path"))
        }

        dir.listFiles()?.forEach { file ->
            if (isMediaFile(file.name)) {
                val song = createSongEntity(file.absolutePath, file.name, "local")
                if (song != null) songs.add(song)
            }
        }

        return ScanResult(songs, errors)
    }

    private fun scanDocumentFile(dir: DocumentFile, songs: MutableList<SongEntity>, errors: MutableList<String>) {
        dir.listFiles().forEach { file ->
            if (file.isDirectory) {
                // Don't recurse too deep - one level only for performance
                file.listFiles().forEach { child ->
                    if (child.name != null && isMediaFile(child.name!!)) {
                                        val song = createSongEntity(child.uri.toString(), child.name!!, "usb")
                        if (song != null) songs.add(song)
                    }
                }
            } else if (file.isFile && file.name != null && isMediaFile(file.name!!)) {
                    val song = createSongEntity(file.uri.toString(), file.name!!, "usb")
                if (song != null) songs.add(song)
            }
        }
    }

    private fun createSongEntity(path: String, fileName: String, source: String): SongEntity? {
        val parser = MetadataParser()
        val (title, artist) = parser.parseFromFilename(fileName)

        val id = hashOf(path)
        return SongEntity(
            id = id,
            title = title,
            artist = artist,
            durationMs = 0,  // Will be updated on first play
            filePath = path,
            source = source
        )
    }

    private fun isMediaFile(name: String): Boolean {
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in SUPPORTED_FORMATS
    }

    companion object {
        val SUPPORTED_FORMATS = setOf("mp4", "mkv", "webm", "avi", "mp3", "m4a", "aac", "ogg", "wav")

        fun hashOf(str: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(str.toByteArray()).take(12).joinToString("") { "%02x".format(it) }
        }
    }
}
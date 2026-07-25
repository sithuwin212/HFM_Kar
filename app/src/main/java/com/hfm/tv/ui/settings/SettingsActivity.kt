package com.hfm.tv.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.documentfile.provider.DocumentFile
import com.hfm.tv.R
import com.hfm.tv.storage.FileScanner

class SettingsActivity : ComponentActivity() {

    companion object {
        const val PICK_FOLDER_REQUEST = 1001
        const val PREFS_NAME = "hfm_settings"
        const val KEY_FOLDER_URI = "folder_uri"
    }

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(androidx.leanback.R.layout.lb_preference_fragment)

        statusText = findViewById(android.R.id.summary)

        // Show saved folder
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedUri = prefs.getString(KEY_FOLDER_URI, null)
        statusText.text = if (savedUri != null) "Folder set ✓" else "Folder ရွေးရန်"

        // Trigger SAF folder picker
        openFolderPicker()
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        startActivityForResult(intent, PICK_FOLDER_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FOLDER_REQUEST && resultCode == Activity.RESULT_OK) {
            val treeUri = data?.data ?: return

            // Persist permission
            contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            // Save URI
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_FOLDER_URI, treeUri.toString())
                .apply()

            statusText.text = "Folder saved! Scanning..."

            // Start scan
            val scanner = FileScanner(this)
            val result = scanner.scanSafFolder(treeUri)

            val db = (application as com.hfm.tv.HFMApplication).database
            Thread {
                db.songDao().insertSongs(result.songs)
            }.start()

            statusText.text = "${result.songs.size} songs found"
        }
    }
}
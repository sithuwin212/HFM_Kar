package com.hfm.tv.ui.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.hfm.tv.R
import com.hfm.tv.data.SongEntity

class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null
    private var vocalOff = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.R.layout.simple_list_item_2)

        val song = intent.getSerializableExtra("song") as? SongEntity
        val titleView = findViewById<TextView>(android.R.id.title)
        val artistView = findViewById<TextView>(android.R.id.text1)

        titleView.text = song.title
        artistView.text = song.artist

        playSong(song)
    }

    private fun playSong(song: SongEntity) {
        player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()

        val mediaItem = MediaItem.fromUri(song.filePath)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onDestroy() {
        player?.release()
        super.onDestroy()
    }

    companion object {
        fun createIntent(context: Context, song: SongEntity): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra("song", song)
            }
        }
    }
}
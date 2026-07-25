package com.hfm.tv.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfm.tv.R
import com.hfm.tv.data.SongEntity
import com.hfm.tv.storage.FileScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var statusView: TextView
    private var adapter: SongAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, saved: Bundle?): View {
        val view = inflater.inflate(androidx.leanback.R.layout.lb_browse_fragment, group, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Simple grid layout for songs
        recyclerView = view.findViewById(android.R.id.list)
        emptyView = view.findViewById(android.R.id.empty)
        statusView = view.findViewById(android.R.id.summary)

        recyclerView.layoutManager = GridLayoutManager(context, 5)

        loadSongs()
    }

    private fun loadSongs() {
        lifecycleScope.launch {
            val db = (activity as? com.hfm.tv.MainActivity)?.database ?: return@launch

            db.songDao().getAllSongs().collect { songs ->
                if (songs.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    statusView.text = "သီချင်းမရှိသေးပါ\n/storage/Karaoke/ ထဲထည့်ပါ"
                } else {
                    emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    if (adapter == null) {
                        adapter = SongAdapter(songs) { song -> playSong(song) }
                        recyclerView.adapter = adapter
                    } else {
                        adapter?.updateSongs(songs)
                    }
                    statusView.text = "${songs.size} သီချင်း"
                }
            }
        }
    }

    private fun playSong(song: SongEntity) {
        val intent = com.hfm.tv.ui.player.PlayerActivity.createIntent(requireContext(), song)
        startActivity(intent)
    }
}

class SongAdapter(
    private var songs: List<SongEntity>,
    private val onPlay: (SongEntity) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(android.R.id.title)
        val artist: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(androidx.leanback.R.layout.lb_row_card_view, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = "${song.artist} - ${song.title}"
        holder.itemView.setOnClickListener { onPlay(song) }
    }

    override fun getItemCount() = songs.size

    fun updateSongs(newSongs: List<SongEntity>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}
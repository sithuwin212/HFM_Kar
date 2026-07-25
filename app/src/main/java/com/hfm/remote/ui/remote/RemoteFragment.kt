package com.hfm.remote.ui.remote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class RemoteFragment : Fragment() {

    private lateinit var statusLabel: TextView

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, saved: Bundle?): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        statusLabel = TextView(requireContext()).apply {
            text = "🎵 HFM Remote"
            setTextSize(24f)
        }
        layout.addView(statusLabel)

        layout.addView(createButton("▶ Play / Pause", "toggle_play"))
        layout.addView(createButton("⏭ Next", "next"))
        layout.addView(createButton("⏮ Previous", "prev"))
        layout.addView(createButton("🎤 Vocal Off", "vocal_off").apply {
            isAllCaps = false
        })
        layout.addView(createButton("🎤 Vocal On", "vocal_on").apply {
            isAllCaps = false
        })

        // Volume control
        val volLayout = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        volLayout.addView(TextView(requireContext()).apply { text = "🔊 Volume" })
        val seekBar = SeekBar(requireContext())
        seekBar.max = 100; seekBar.progress = 70
        seekBar.setOnSeekBarChangeChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, v: Int, fromUser: Boolean) {
                if (fromUser) {
                    val act = requireActivity() as? com.hfm.remote.MainActivity
                    act?.sendCommand("volume", mapOf("level" to v))
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        volLayout.addView(seekBar)
        layout.addView(volLayout)

        layout.addView(createButton("📡 Reconnect", "reconnect"))

        return layout
    }

    private fun createButton(text: String, command: String): Button {
        return Button(requireContext()).apply {
            this.text = text
            setOnClickListener {
                val act = requireActivity() as? com.hfm.remote.MainActivity
                act?.sendCommand(command)
            }
        }
    }
}

// Fix for SeekBar listener naming
fun SeekBar.onSeekBarChange(l: SeekBar.OnSeekBarChangeListener) {
    setOnSeekBarChangeListener(l)
}
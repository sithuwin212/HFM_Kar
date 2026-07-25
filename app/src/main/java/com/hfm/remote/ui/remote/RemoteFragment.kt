package com.hfm.remote.ui.remote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class RemoteFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, saved: Bundle?): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        layout.addView(TextView(requireContext()).apply { text = "🎵 HFM Remote"; setTextSize(24f) })

        layout.addView(Button(requireContext()).apply { text = "▶ Play/Pause"; setOnClickListener { sendCmd("toggle_play") } })
        layout.addView(Button(requireContext()).apply { text = "⏭ Next"; setOnClickListener { sendCmd("next") } })
        layout.addView(Button(requireContext()).apply { text = "⏮ Previous"; setOnClickListener { sendCmd("prev") } })
        layout.addView(Button(requireContext()).apply { text = "🎤 Vocal Off"; setOnClickListener { sendCmd("vocal_off") } })
        layout.addView(Button(requireContext()).apply { text = "🎤 Vocal On"; setOnClickListener { sendCmd("vocal_on") } })
        layout.addView(Button(requireContext()).apply { text = "📡 Reconnect"; setOnClickListener { sendCmd("reconnect") } })

        val volLayout = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        volLayout.addView(TextView(requireContext()).apply { text = "🔊" })
        val seekBar = SeekBar(requireContext())
        seekBar.max = 100; seekBar.progress = 70
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, v: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {
                sb?.let { sendCmd("volume", mapOf("level" to it.progress)) }
            }
        })
        volLayout.addView(seekBar)
        layout.addView(volLayout)

        return layout
    }

    private fun sendCmd(type: String, data: Map<String, Any> = emptyMap()) {
        (requireActivity() as? com.hfm.remote.MainActivity)?.sendCommand(type, data)
    }
}
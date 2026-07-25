package com.hfm.remote.ui.queue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class QueueFragment : Fragment() {

    private val queueItems = mutableListOf<String>()
    private lateinit var listView: ListView

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, saved: Bundle?): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
        }

        val header = TextView(requireContext()).apply {
            text = "📋 Queue"
            setTextSize(22f)
        }
        layout.addView(header)

        listView = ListView(requireContext())
        layout.addView(listView)

        val clearBtn = Button(requireContext()).apply {
            text = "🗑 Clear Queue"
            setOnClickListener {
                val act = requireActivity() as? com.hfm.remote.MainActivity
                act?.sendCommand("queue_clear")
            }
        }
        layout.addView(clearBtn)

        return layout
    }
}
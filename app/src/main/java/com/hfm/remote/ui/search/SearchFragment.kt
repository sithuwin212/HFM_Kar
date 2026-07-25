package com.hfm.remote.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class SearchFragment : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var resultList: ListView

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, saved: Bundle?): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
        }

        searchInput = EditText(requireContext()).apply {
            hint = "Search songs, artists..."
            setTextSize(18f)
        }
        layout.addView(searchInput)

        val btnSearch = Button(requireContext()).apply {
            text = "🔍 Search"
            setOnClickListener { performSearch(searchInput.text.toString()) }
        }
        layout.addView(btnSearch)

        val btnScan = Button(requireContext()).apply {
            text = "📷 Scan QR (Connect to TV)"
            setOnClickListener { scanQR() }
        }
        layout.addView(btnScan)

        resultList = ListView(requireContext())
        layout.addView(resultList)

        return layout
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        val activity = requireActivity() as? com.hfm.remote.MainActivity ?: return
        activity.sendCommand("search", mapOf("q" to query))
        Toast.makeText(context, "Searching: $query", Toast.LENGTH_SHORT).show()
    }

    private fun scanQR() {
        // Launch QR scanner — will use zxing embedded scanner
        val intent = com.journeyapps.barcodescanner.ScanContract()
        // Simplified: just show a message for now
        Toast.makeText(context, "QR Scanner ready", Toast.LENGTH_SHORT).show()
    }
}
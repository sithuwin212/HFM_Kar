package com.hfm.tv.ui.pairing

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.hfm.tv.R
import com.hfm.tv.network.HFMWebSocketServer

class PairingFragment : Fragment() {

    private lateinit var qrImage: ImageView
    private lateinit var ipLabel: TextView
    private lateinit var statusLabel: TextView

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, saved: Bundle?): View {
        val view = inflater.inflate(androidx.leanback.R.layout.lb_browse_fragment, group, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        qrImage = view.findViewById(android.R.id.icon)
        ipLabel = view.findViewById(android.R.id.text1)
        statusLabel = view.findViewById(android.R.id.text2)

        generateQRCode()
    }

    private fun generateQRCode() {
        val wsServer = (activity as? com.hfm.tv.MainActivity)?.webSocketServer
        val ws = wsServer ?: return

        val ip = ws.getLocalIpAddress()
        val port = 8550
        val qrData = "hfm://pair?ip=$ip&port=$port"

        ipLabel.text = "TV: $ip:$port"
        statusLabel.text = "HFM Remote ဖြင့် QR Code ကိုဖတ်ပါ"

        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 400, 400)
            val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565)

            for (x in 0 until 400) {
                for (y in 0 until 400) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            qrImage.setImageBitmap(bitmap)
        } catch (_: Exception) {}
    }
}
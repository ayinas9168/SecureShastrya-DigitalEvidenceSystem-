package com.example.secureshastrya

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secureshastrya.data.AppDatabase
import com.example.secureshastrya.databinding.ActivityEvidenceViewerBinding
import com.example.secureshastrya.util.KeystoreManager
import com.example.secureshastrya.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.crypto.spec.SecretKeySpec

class EvidenceViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEvidenceViewerBinding
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvidenceViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val evidenceId = intent.getIntExtra("evidenceId", -1)
        if (evidenceId == -1) {
            Toast.makeText(this, "Error: Invalid evidence ID.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@EvidenceViewerActivity)
            val evidence = db.evidenceDao().getEvidenceById(evidenceId)
            
            if (evidence == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EvidenceViewerActivity, "Error: Evidence not found.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@launch
            }

            val file = File(filesDir, evidence.filename)
            if (!file.exists()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EvidenceViewerActivity, "Error: Evidence file not found.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@launch
            }

            try {
                val encryptedData = file.readBytes()
                
                // Decrypt
                val encryptedKeyBytes = Base64.decode(evidence.encryptionKey, Base64.NO_WRAP)
                val keyIvBytes = Base64.decode(evidence.keyIv, Base64.NO_WRAP)
                val rawKeyBytes = KeystoreManager.decrypt(encryptedKeyBytes, keyIvBytes)
                val secretKey = SecretKeySpec(rawKeyBytes, "AES")
                val fileIvBytes = Base64.decode(evidence.iv, Base64.NO_WRAP)
                val decryptedData = SecurityUtils.decryptData(encryptedData, secretKey, fileIvBytes)
                
                withContext(Dispatchers.Main) {
                    if (evidence.mediaType.startsWith("image/")) {
                        val bitmap = BitmapFactory.decodeByteArray(decryptedData, 0, decryptedData.size)
                        binding.evidenceImageView.setImageBitmap(bitmap)
                        binding.evidenceImageView.visibility = View.VISIBLE
                    } else if (evidence.mediaType.startsWith("audio/")) {
                        setupAudioPlayer(decryptedData)
                    }
                }
            } catch (e: Exception) {
                Log.e("EvidenceViewer", "Error viewing evidence", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EvidenceViewerActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupAudioPlayer(data: ByteArray) {
        binding.llAudioPlayer.visibility = View.VISIBLE
        binding.btnPlayAudio.setOnClickListener {
            try {
                // Save to temp file for MediaPlayer
                val tempFile = File.createTempFile("playing_audio", ".amr", cacheDir)
                FileOutputStream(tempFile).use { it.write(data) }
                
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    prepare()
                    start()
                }
                Toast.makeText(this, "Playing audio...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Audio playback failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

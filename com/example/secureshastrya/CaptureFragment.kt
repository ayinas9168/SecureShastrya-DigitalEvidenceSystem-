package com.example.secureshastrya

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.secureshastrya.data.Evidence
import com.example.secureshastrya.databinding.FragmentCaptureBinding
import com.example.secureshastrya.util.KeystoreManager
import com.example.secureshastrya.util.LocationUtils
import com.example.secureshastrya.util.SecurityUtils
import com.example.secureshastrya.util.SessionManager
import com.example.secureshastrya.viewmodel.AuditLogViewModel
import com.example.secureshastrya.viewmodel.BlockchainViewModel
import com.example.secureshastrya.viewmodel.CaptureViewModel
import com.example.secureshastrya.viewmodel.ViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CaptureFragment : Fragment() {

    private var _binding: FragmentCaptureBinding? = null
    private val binding get() = _binding!!

    private lateinit var captureViewModel: CaptureViewModel
    private lateinit var auditLogViewModel: AuditLogViewModel
    private lateinit var sessionManager: SessionManager

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecordingAudio = false

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Permissions not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureBinding.inflate(inflater, container, false)
        val factory = ViewModelFactory(requireActivity().application as SecureShastryaApplication)
        captureViewModel = ViewModelProvider(this, factory).get(CaptureViewModel::class.java)
        auditLogViewModel = ViewModelProvider(this).get(AuditLogViewModel::class.java)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestMultiplePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }

        binding.imageCaptureButton.setOnClickListener { takePhoto() }

        binding.btnAudioCapture.setOnClickListener {
            if (!isRecordingAudio) startRecordingAudio() else stopRecordingAudio()
        }
    }

    private fun startRecordingAudio() {
        try {
            audioFile = File(requireContext().filesDir, "audio_${System.currentTimeMillis()}.amr")
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }
            isRecordingAudio = true
            binding.btnAudioCapture.setImageResource(android.R.drawable.ic_media_pause)
            binding.integrityStatus.text = "RECORDING AUDIO... (Tap to Stop)"
            Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Recording failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecordingAudio() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecordingAudio = false
            binding.btnAudioCapture.setImageResource(R.drawable.ic_mic)
            binding.integrityStatus.text = getString(R.string.status_ready)
            
            audioFile?.let { file ->
                showNamingDialog { name -> processCapturedFile(file, name, "audio/amr") }
            }
        } catch (e: Exception) {
            Log.e("Capture", "Error stopping audio", e)
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(requireContext().filesDir, "photo_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    auditLogViewModel.addLog("CAPTURE_FAIL", "Photo failed: ${exc.message}")
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    showNamingDialog { name -> processCapturedFile(photoFile, name, "image/jpeg") }
                }
            })
    }

    private fun showNamingDialog(onNameEntered: (String) -> Unit) {
        val editText = EditText(requireContext())
        editText.hint = "Name this evidence"
        AlertDialog.Builder(requireContext())
            .setTitle("Save Evidence")
            .setView(editText)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->
                val name = editText.text.toString().trim()
                onNameEntered(if (name.isNotEmpty()) name else "Unnamed Evidence")
            }
            .show()
    }

    private fun processCapturedFile(file: File, name: String, mediaType: String) {
        cameraExecutor.execute {
            val fileBytes = file.readBytes()
            val sha256Hash = SecurityUtils.sha256(fileBytes)
            val dataKey = SecurityUtils.generateAESKey()
            val (iv, encryptedData) = SecurityUtils.encryptData(fileBytes, dataKey)
            val (keyIv, encryptedKey) = KeystoreManager.encrypt(dataKey.encoded)

            FileOutputStream(file).use { it.write(encryptedData) }

            val evidence = Evidence(
                caseId = sessionManager.getUserId(),
                name = name,
                filename = file.name,
                mediaType = mediaType,
                sha256Hash = sha256Hash,
                gpsCoordinates = "Detecting...",
                timestamp = System.currentTimeMillis(),
                encryptionKey = Base64.encodeToString(encryptedKey, Base64.NO_WRAP),
                iv = Base64.encodeToString(iv, Base64.NO_WRAP),
                keyIv = Base64.encodeToString(keyIv, Base64.NO_WRAP)
            )
            captureViewModel.addEvidence(evidence, file)
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Saved & Secured", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (exc: Exception) {}
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        mediaRecorder?.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO
        )
    }
}

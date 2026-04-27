package com.example.secureshastrya

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secureshastrya.adapter.EvidenceAdapter
import com.example.secureshastrya.data.Evidence
import com.example.secureshastrya.databinding.FragmentLockerBinding
import com.example.secureshastrya.util.KeystoreManager
import com.example.secureshastrya.util.SecurityUtils
import com.example.secureshastrya.util.SessionManager
import com.example.secureshastrya.viewmodel.LockerViewModel
import com.example.secureshastrya.viewmodel.ViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor

class LockerFragment : Fragment() {

    private var _binding: FragmentLockerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LockerViewModel
    private lateinit var evidenceAdapter: EvidenceAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                showNamingDialog { name ->
                    processUploadedFile(uri, name)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLockerBinding.inflate(inflater, container, false)
        val factory = ViewModelFactory(requireActivity().application as SecureShastryaApplication)
        viewModel = ViewModelProvider(this, factory).get(LockerViewModel::class.java)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        
        executor = ContextCompat.getMainExecutor(requireContext())
        
        val currentUserId = sessionManager.getUserId()
        if (sessionManager.isBiometricEnabled(currentUserId)) {
            setupBiometricPrompt()
            biometricPrompt.authenticate(promptInfo)
        } else {
            observeEvidence()
        }

        binding.btnUploadLocker.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pickFileLauncher.launch(intent)
        }

        binding.btnSelectEvidence.setOnClickListener {
            toggleSelectionMode()
        }

        binding.btnFileComplaint.setOnClickListener {
            if (evidenceAdapter.selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Select at least one evidence", Toast.LENGTH_SHORT).show()
            } else {
                showComplaintDialog()
            }
        }
    }

    private fun toggleSelectionMode() {
        evidenceAdapter.isSelectionMode = !evidenceAdapter.isSelectionMode
        if (evidenceAdapter.isSelectionMode) {
            binding.btnSelectEvidence.text = "Cancel"
            binding.btnFileComplaint.visibility = View.VISIBLE
            binding.btnUploadLocker.visibility = View.GONE
        } else {
            binding.btnSelectEvidence.text = "Select"
            binding.btnFileComplaint.visibility = View.GONE
            binding.btnUploadLocker.visibility = View.VISIBLE
        }
    }

    private fun showComplaintDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etTitle = EditText(requireContext()).apply { hint = "Complaint Title" }
        val etDesc = EditText(requireContext()).apply { hint = "Description/Details" }
        
        layout.addView(etTitle)
        layout.addView(etDesc)

        AlertDialog.Builder(requireContext())
            .setTitle("File Official Complaint")
            .setMessage("Selected ${evidenceAdapter.selectedItems.size} items. This action is permanent and cannot be deleted.")
            .setView(layout)
            .setPositiveButton("File Now") { _, _ ->
                val title = etTitle.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                if (title.isNotEmpty() && desc.isNotEmpty()) {
                    viewModel.fileComplaint(title, desc, sessionManager.getUserId(), evidenceAdapter.selectedItems.toList())
                    Toast.makeText(requireContext(), "Complaint filed successfully!", Toast.LENGTH_LONG).show()
                    toggleSelectionMode()
                } else {
                    Toast.makeText(requireContext(), "Please fill all details", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(requireContext(), "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    observeEvidence()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setNegativeButtonText(getString(R.string.biometric_negative_button))
            .build()
    }

    private fun setupRecyclerView() {
        evidenceAdapter = EvidenceAdapter()
        binding.evidenceRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = evidenceAdapter
        }
    }

    private fun observeEvidence() {
        val currentUserId = sessionManager.getUserId()
        viewModel.getEvidenceForCase(currentUserId).observe(viewLifecycleOwner) { evidenceList ->
            if (evidenceList.isNullOrEmpty()) {
                binding.noEvidenceTextView.visibility = View.VISIBLE
                binding.evidenceRecyclerView.visibility = View.GONE
            } else {
                binding.noEvidenceTextView.visibility = View.GONE
                binding.evidenceRecyclerView.visibility = View.VISIBLE
                evidenceAdapter.submitList(evidenceList)
            }
        }
    }

    private fun showNamingDialog(onNameEntered: (String) -> Unit) {
        val editText = EditText(requireContext())
        editText.hint = getString(R.string.hint_evidence_name)
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.name_evidence))
            .setView(editText)
            .setPositiveButton(getString(R.string.upload)) { _, _ ->
                val name = editText.text.toString().trim()
                onNameEntered(if (name.isNotEmpty()) name else getString(R.string.uploaded_evidence))
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun processUploadedFile(uri: Uri, name: String) {
        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val bytes = inputStream.readBytes()
        inputStream.close()

        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val fileName = "upload_${System.currentTimeMillis()}"

        val currentUserId = sessionManager.getUserId()
        if (!sessionManager.isBiometricEnabled(currentUserId)) {
            sessionManager.setBiometricEnabled(currentUserId, true)
            Toast.makeText(requireContext(), "Biometric security enabled for your evidence", Toast.LENGTH_LONG).show()
        }

        val sha256Hash = SecurityUtils.sha256(bytes)
        val dataKey = SecurityUtils.generateAESKey()
        val (iv, encryptedData) = SecurityUtils.encryptData(bytes, dataKey)
        val (keyIv, encryptedKey) = KeystoreManager.encrypt(dataKey.encoded)

        val file = File(requireContext().filesDir, fileName)
        FileOutputStream(file).use { it.write(encryptedData) }

        val evidence = Evidence(
            caseId = currentUserId,
            name = name,
            filename = fileName,
            mediaType = mimeType,
            sha256Hash = sha256Hash,
            gpsCoordinates = "Uploaded",
            timestamp = System.currentTimeMillis(),
            encryptionKey = Base64.encodeToString(encryptedKey, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP),
            keyIv = Base64.encodeToString(keyIv, Base64.NO_WRAP)
        )

        viewModel.addEvidence(evidence)
        Toast.makeText(requireContext(), "Evidence uploaded successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
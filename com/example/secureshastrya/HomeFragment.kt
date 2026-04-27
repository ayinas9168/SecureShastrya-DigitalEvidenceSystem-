package com.example.secureshastrya

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.secureshastrya.data.AppDatabase
import com.example.secureshastrya.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnVerifyIntegrity.setOnClickListener {
            verifyIntegrity()
        }
    }

    private fun verifyIntegrity() {
        binding.tvIntegrityStatus.text = getString(R.string.verifying_blockchain)
        binding.tvIntegrityStatus.setTextColor(Color.WHITE)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val blocks = db.blockchainDao().getAllBlocks()
            val evidence = db.evidenceDao().getAllEvidenceSync()

            var isChainValid = true
            var lastHash = "0"

            if (blocks.isEmpty()) {
                binding.tvIntegrityStatus.text = getString(R.string.status_no_evidence)
                return@launch
            }

            for (block in blocks) {
                // Verify block content integrity
                val blockData = "${block.blockIndex}${block.timestamp}${block.evidenceHash}${block.previousBlockHash}"
                val calculatedTag = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(blockData.toByteArray())
                    .joinToString("") { "%02x".format(it) }

                if (calculatedTag != block.integrityTag || block.previousBlockHash != lastHash) {
                    isChainValid = false
                    break
                }
                lastHash = block.integrityTag

                // Verify that the evidence matching this hash still exists and hasn't changed
                val matchingEvidence = evidence.find { it.sha256Hash == block.evidenceHash }
                if (matchingEvidence == null) {
                    isChainValid = false
                    break
                }
            }

            if (isChainValid) {
                binding.tvIntegrityStatus.text = getString(R.string.status_verified)
                binding.tvIntegrityStatus.setTextColor(Color.GREEN)
            } else {
                binding.tvIntegrityStatus.text = getString(R.string.status_tamper_alert)
                binding.tvIntegrityStatus.setTextColor(Color.RED)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
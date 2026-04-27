package com.example.secureshastrya

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.secureshastrya.auth.AuthActivity
import com.example.secureshastrya.data.User
import com.example.secureshastrya.databinding.FragmentProfileBinding
import com.example.secureshastrya.viewmodel.ViewModelFactory
import java.util.concurrent.Executor

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var executor: Executor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val factory = ViewModelFactory(requireActivity().application as SecureShastryaApplication)
        viewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)
        executor = ContextCompat.getMainExecutor(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let { displayUserData(it) }
        }

        binding.switchBiometric.isChecked = viewModel.isBiometricEnabled()

        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBiometricAvailabilityAndPrompt()
            } else {
                viewModel.setBiometricEnabled(false)
                Toast.makeText(requireContext(), "Biometric login disabled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireActivity(), AuthActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun checkBiometricAvailabilityAndPrompt() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }
            else -> {
                Toast.makeText(requireContext(), "Biometric features are not available on this device", Toast.LENGTH_LONG).show()
                binding.switchBiometric.isChecked = false
            }
        }
    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enable Biometric Security")
            .setSubtitle("Confirm your identity to enable biometric login")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    binding.switchBiometric.isChecked = false
                    Toast.makeText(requireContext(), "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.setBiometricEnabled(true)
                    Toast.makeText(requireContext(), "Biometric security enabled", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Prompt stays open for retry
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun displayUserData(user: User) {
        binding.tvName.text = user.username ?: "User"
        binding.tvEmail.text = user.email
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
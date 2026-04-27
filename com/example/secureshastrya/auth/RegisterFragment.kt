package com.example.secureshastrya.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.secureshastrya.MainActivity
import com.example.secureshastrya.R
import com.example.secureshastrya.SecureShastryaApplication
import com.example.secureshastrya.data.UserRole
import com.example.secureshastrya.databinding.FragmentRegisterBinding
import com.example.secureshastrya.util.SessionManager
import com.example.secureshastrya.viewmodel.RegisterViewModel
import com.example.secureshastrya.viewmodel.RegistrationResult
import com.example.secureshastrya.viewmodel.ViewModelFactory
import java.util.concurrent.Executor

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RegisterViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var executor: Executor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val factory = ViewModelFactory(requireActivity().application as SecureShastryaApplication)
        viewModel = ViewModelProvider(this, factory).get(RegisterViewModel::class.java)
        sessionManager = SessionManager(requireContext())
        executor = ContextCompat.getMainExecutor(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbJudge) {
                Toast.makeText(requireContext(), "Judge registration requires pre-authorized email", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val role = if (binding.rbJudge.isChecked) UserRole.JUDGE else UserRole.USER

            if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                viewModel.register(name, email, phone, password, role)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.registrationResult.observe(viewLifecycleOwner) {
            when (it) {
                is RegistrationResult.Success -> {
                    sessionManager.saveUserId(it.userId)
                    checkBiometricAvailabilityAndPrompt(it.userId)
                }
                is RegistrationResult.Error -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            } catch (e: Exception) {
                if (!findNavController().popBackStack()) {
                    findNavController().navigate(R.id.loginFragment)
                }
            }
        }
    }

    private fun checkBiometricAvailabilityAndPrompt(userId: Int) {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt(userId)
            }
            else -> {
                navigateToMain()
            }
        }
    }

    private fun showBiometricPrompt(userId: Int) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enable Biometric Security")
            .setSubtitle("Use your fingerprint or face recognition to secure your account")
            .setNegativeButtonText("Skip")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If they skip or it fails, we still finish registration
                    navigateToMain()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    sessionManager.setBiometricEnabled(userId, true)
                    Toast.makeText(requireContext(), "Biometric security enabled", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun navigateToMain() {
        Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
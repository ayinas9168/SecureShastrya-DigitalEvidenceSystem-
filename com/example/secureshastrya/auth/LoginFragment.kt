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
import com.example.secureshastrya.databinding.FragmentLoginBinding
import com.example.secureshastrya.util.SessionManager
import com.example.secureshastrya.viewmodel.LoginViewModel
import com.example.secureshastrya.viewmodel.LoginResult
import com.example.secureshastrya.viewmodel.ViewModelFactory
import java.util.concurrent.Executor

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var executor: Executor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val factory = ViewModelFactory(requireActivity().application as SecureShastryaApplication)
        viewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)
        sessionManager = SessionManager(requireContext())
        executor = ContextCompat.getMainExecutor(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Option 1: Password Login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(requireContext(), "Please enter credentials", Toast.LENGTH_SHORT).show()
            }
        }

        // Option 2: Biometric Login
        setupBiometricUI()

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        observeViewModel()
    }

    private fun setupBiometricUI() {
        val biometricManager = BiometricManager.from(requireContext())
        val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        
        // Use the LAST known user ID to check if biometrics were enabled
        val lastUserId = sessionManager.getLastUserId()
        val isEnabled = if (lastUserId != -1) sessionManager.isBiometricEnabled(lastUserId) else false

        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS && isEnabled) {
            binding.btnBiometricLogin.visibility = View.VISIBLE
            binding.divider.visibility = View.VISIBLE
            binding.tvOr.visibility = View.VISIBLE
            
            binding.btnBiometricLogin.setOnClickListener {
                showBiometricPrompt(lastUserId)
            }
        } else {
            binding.btnBiometricLogin.visibility = View.GONE
            binding.divider.visibility = View.GONE
            binding.tvOr.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is LoginResult.Success -> {
                    sessionManager.saveUserId(result.userId)
                    navigateToMain()
                }
                is LoginResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun showBiometricPrompt(userId: Int) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your fingerprint or face")
            .setNegativeButtonText("Use Password")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Success! Restore the user ID and navigate
                    sessionManager.saveUserId(userId)
                    navigateToMain()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun navigateToMain() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.falls.remnants.ui.tools

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.falls.remnants.data.Configs
import com.falls.remnants.data.Utils
import com.falls.remnants.databinding.FragmentToolsBinding

class ToolsFragment : Fragment() {

    private var _binding: FragmentToolsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(ToolsViewModel::class.java)

        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observe logged in status
        Configs.loggedIn.observe(viewLifecycleOwner) {
            if (it) {
                binding.logoutButton.isEnabled = true
                binding.loginButton.isEnabled = false
            } else {
                binding.logoutButton.isEnabled = false
                binding.loginButton.isEnabled = true
            }
        }

        // Observe username
        Configs.username.observe(viewLifecycleOwner) {
            if (it.isNotBlank()) {
                binding.username.text = "Welcome, " + it
            } else {
                binding.username.text = "Not logged in"
            }
        }

        // Click listeners
        binding.loginButton.setOnClickListener {
            onLoginButtonPressed()
        }
        binding.logoutButton.setOnClickListener {
            onLogoutButtonPressed()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onLoginButtonPressed() {

        // Go to URL when clicked
        val url = "https://anilist.co/api/v2/oauth/authorize?client_id=${Configs.client_id}&response_type=token"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)

        // Log in configuration is done within activity re-initialization
    }

    private fun onLogoutButtonPressed() {

        Configs.username.value = ""
        Configs.token = ""
        Configs.loggedIn.value = false

        Utils.saveSharedSettings(requireActivity(), "username", "")
        Utils.saveSharedSettings(requireActivity(), "access_token", "")

        Toast.makeText(context, "You are now logged out", Toast.LENGTH_SHORT).show()
    }
}
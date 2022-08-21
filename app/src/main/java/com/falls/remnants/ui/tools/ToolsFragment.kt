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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.falls.remnants.adapter.AdapterClickListener
import com.falls.remnants.adapter.MediaListAdapter
import com.falls.remnants.adapter.MediaViewType
import com.falls.remnants.data.Configs
import com.falls.remnants.data.Utils
import com.falls.remnants.databinding.FragmentToolsBinding
import com.falls.remnants.ui.browse.BrowseFragmentDirections
import timber.log.Timber

class ToolsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentToolsBinding? = null
    private lateinit var adapter: MediaListAdapter
    private lateinit var notificationsViewModel: ToolsViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        notificationsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)

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
                binding.username.text = "" + it
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

        // Data loading
        notificationsViewModel.status.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(context, "Data loaded", Toast.LENGTH_SHORT).show()
                notificationsViewModel.status.value = false
            }
        }

        // Set visible column count
        Configs.columns.observe(viewLifecycleOwner) { columns ->
            binding.recyclerView.layoutManager =
                GridLayoutManager(requireContext(), columns)
        }

        // Recycler view adapter
        adapter = MediaListAdapter(
            AdapterClickListener {
                val action =
                    ToolsFragmentDirections.actionNavigationNotificationsToAnimeDetailsFragment(it)
                findNavController().navigate(action)
            }, MediaViewType.SEARCH
        )
        binding.recyclerView.adapter = adapter

        notificationsViewModel.animeRemnants.observe(viewLifecycleOwner) { anime ->
            anime?.let {
                adapter.submitList(anime)
            }
        }

        // Pull to refresh
        binding.swipeRefreshLayout2.setOnRefreshListener(this)

        return binding.root
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

    // Pull to refresh
    override fun onRefresh() {
        if (notificationsViewModel.status.value == false) {
            notificationsViewModel.refreshList()
            binding.swipeRefreshLayout2.isRefreshing = false
        } else {
            binding.swipeRefreshLayout2.isRefreshing = false
        }
    }
}
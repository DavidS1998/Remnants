package com.falls.remnants.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.falls.remnants.adapter.AdapterClickListener
import com.falls.remnants.adapter.MediaListAdapter
import com.falls.remnants.adapter.MediaViewType
import com.falls.remnants.data.Configs
import com.falls.remnants.databinding.FragmentLibraryCurrentListBinding
import timber.log.Timber

class TabCurrentFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentLibraryCurrentListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LibraryViewModel by activityViewModels()
    private lateinit var adapter: MediaListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryCurrentListBinding.inflate(inflater, container, false)

        // Recycler view adapter
        adapter = MediaListAdapter(
            AdapterClickListener {
                val action =
                    LibraryFragmentDirections.actionNavigationDashboardToAnimeDetailsFragment(it)
                findNavController().navigate(action)
            }, MediaViewType.SEASONAL
        )

        binding.recyclerView.adapter = adapter

        viewModel.animeCurrent.observe(viewLifecycleOwner) { anime ->
            anime?.let {
                adapter.submitList(anime)
                binding.recyclerView.scrollToPosition(0)
            }
        }
        binding.viewModel = viewModel

        // Set visible column count
        Configs.columns.observe(viewLifecycleOwner) { columns ->
            binding.recyclerView.layoutManager =
                GridLayoutManager(requireContext(), columns + 1)
            onRefresh()
        }

        // Pull to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        return binding.root
    }

    // For pull to refresh
    override fun onRefresh() {
        viewModel.refreshList(MediaViewType.SEASONAL)
        binding.swipeRefreshLayout.isRefreshing = false
    }
}
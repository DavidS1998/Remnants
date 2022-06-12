package com.falls.remnants.ui.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.falls.remnants.adapter.AdapterClickListener
import com.falls.remnants.adapter.MediaListAdapter
import com.falls.remnants.adapter.MediaViewType
import com.falls.remnants.data.Configs
import com.falls.remnants.databinding.FragmentBrowseSearchListBinding

public class TabSearchFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentBrowseSearchListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BrowseViewModel by activityViewModels()
    private lateinit var adapter: MediaListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowseSearchListBinding.inflate(inflater, container, false)

        // Recycler view adapter
        adapter = MediaListAdapter(
            AdapterClickListener {
                val action =
                    BrowseFragmentDirections.actionNavigationHomeToAnimeDetailsFragment(it)
                findNavController().navigate(action)
            }, MediaViewType.SEARCH
        )

        binding.recyclerView.adapter = adapter

        viewModel.animeSearch.observe(viewLifecycleOwner) { anime ->
            anime?.let {
                adapter.submitList(anime)
                // Scroll to top on new search
                binding.recyclerView.scrollToPosition(0)
            }
        }
        binding.viewModel = viewModel

        // Set visible column count
        Configs.columns.observe(viewLifecycleOwner) { columns ->
            binding.recyclerView.layoutManager =
                GridLayoutManager(requireContext(), columns)
        }

        // Observe logged in status
        viewModel.animeSearch.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.searchIndicator.visibility = View.VISIBLE
            } else {
                binding.searchIndicator.visibility = View.GONE
            }
        }

        // Pull to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        return binding.root
    }

    // For pull to refresh
    override fun onRefresh() {
        viewModel.refreshList(MediaViewType.SEARCH)
        binding.swipeRefreshLayout.isRefreshing = false
    }
}

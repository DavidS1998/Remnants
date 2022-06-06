package com.falls.remnants.ui.browse

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
import com.falls.remnants.databinding.FragmentBrowseUpcomingListBinding
import timber.log.Timber

class TabUpcomingFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentBrowseUpcomingListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BrowseViewModel by activityViewModels()
    private lateinit var adapter: MediaListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowseUpcomingListBinding.inflate(inflater, container, false)

        // Recycler view adapter
        adapter = MediaListAdapter(
            AdapterClickListener {
                val action =
                    BrowseFragmentDirections.actionNavigationHomeToAnimeDetailsFragment(it)
                findNavController().navigate(action)
            }, MediaViewType.UPCOMING
        )

        binding.recyclerView.adapter = adapter

        viewModel.animeUpcoming.observe(viewLifecycleOwner) { anime ->
            anime?.let {
                adapter.submitList(anime)
            }
        }
        binding.viewModel = viewModel

        // Set visible column count
        Configs.columns.observe(viewLifecycleOwner) { columns ->
            binding.recyclerView.layoutManager =
                GridLayoutManager(requireContext(), columns)
            onRefresh()
        }

        // Endless scroller
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val totalItemCount = recyclerView.layoutManager!!.itemCount
                val lastVisibleItem =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                // Load more when the user has reached the latest loaded page
                if (lastVisibleItem >= totalItemCount - 10) {
                    Timber.d("Reached ${lastVisibleItem}/${totalItemCount}")
                    viewModel.getMedia(MediaViewType.UPCOMING)
                }
            }
        }
        binding.recyclerView.addOnScrollListener(scrollListener)



        // Pull to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        return binding.root
    }

    // For pull to refresh
    override fun onRefresh() {
        viewModel.refreshList(MediaViewType.UPCOMING)
        binding.swipeRefreshLayout.isRefreshing = false
    }
}

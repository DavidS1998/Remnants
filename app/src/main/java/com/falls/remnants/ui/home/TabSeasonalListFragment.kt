package com.falls.remnants.ui.home

import android.os.Bundle
import android.view.*
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.falls.remnants.R
import com.falls.remnants.databinding.FragmentSeasonalListBinding
import com.falls.remnants.recycler.SeasonalListAdapter
import com.falls.remnants.recycler.SeasonalListClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber

class TabSeasonalListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentSeasonalListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TabSeasonalListViewModel by activityViewModels()
    private lateinit var adapter: SeasonalListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSeasonalListBinding.inflate(inflater, container, false)

        // Recycler view
        adapter = SeasonalListAdapter(SeasonalListClickListener { anime ->
            viewModel.navigateToDetail(anime)
        })
        binding.seasonalListRV.adapter = adapter

        viewModel.anime.observe(viewLifecycleOwner) { anime ->
            anime?.let {
                adapter.submitList(anime)
            }
        }
        binding.viewModel = viewModel

        // Navigate to details
        viewModel.navigateToDetail.observe(viewLifecycleOwner) { anime ->
            anime?.let {
                this.findNavController().navigate(
                    HomeFragmentDirections.actionNavigationHomeToAnimeDetailsFragment(anime)
                )
                viewModel.onNavigatedToDetail()
            }
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
                    viewModel.getMedia()
                }
            }
        }
        binding.seasonalListRV.addOnScrollListener(scrollListener)

        // Set visible column count
        viewModel.columns.observe(viewLifecycleOwner) { columns ->
            binding.seasonalListRV.layoutManager =
                GridLayoutManager(requireContext(), columns + 1)
            onRefresh()
        }

        // Pull to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        return binding.root
    }

    // For pull to refresh
    override fun onRefresh() {
        viewModel.refreshList()
        binding.swipeRefreshLayout.isRefreshing = false
    }
}
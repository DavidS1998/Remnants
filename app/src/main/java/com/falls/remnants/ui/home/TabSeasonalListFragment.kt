package com.falls.remnants.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.falls.remnants.databinding.FragmentSeasonalListBinding
import com.falls.remnants.recycler.SeasonalListAdapter
import timber.log.Timber

class TabSeasonalListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentSeasonalListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel: TabSeasonalListViewModel
    private lateinit var adapter: SeasonalListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSeasonalListBinding.inflate(inflater, container, false)
        val toolbar = (activity as AppCompatActivity).supportActionBar!!

        // Initialize the viewmodel
        val application = requireNotNull(this.activity).application
        viewModel = TabSeasonalListViewModel(binding, application)

        // Recycler view
        adapter = SeasonalListAdapter()
        binding.seasonalListRV.adapter = adapter

        viewModel.anime.observe(viewLifecycleOwner) {anime ->
            anime?.let {
                adapter.submitList(anime)
//                binding.textHome.text = anime.joinToString { "\n" + it.engTitle }
            }
        }

        // Endless scroller
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val totalItemCount = recyclerView.layoutManager!!.itemCount
                val lastVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                // Load more when the user has reached the latest loaded page
                if (lastVisibleItem >= totalItemCount - 10) {
                    Timber.d("Reached ${lastVisibleItem}/${totalItemCount}")
                    viewModel.getMedia()
                }
            }

            @SuppressLint("RestrictedApi")
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) {
//                    toolbar.hide()

                } else if (dy < 0) {
//                    toolbar.show()
                }
            }
        }
        binding.seasonalListRV.setOnScrollListener(scrollListener)

        // Toolbar
        toolbar.title = "Home"

        // Swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        return binding.root
    }

    override fun onRefresh() {
        Timber.d("Refreshing...")
        viewModel.emptyList()
        viewModel.getMedia()
        binding.swipeRefreshLayout.isRefreshing = false
    }
}
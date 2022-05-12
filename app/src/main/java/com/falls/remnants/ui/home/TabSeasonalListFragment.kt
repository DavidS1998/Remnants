package com.falls.remnants.ui.home

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
    private lateinit var viewModel: TabSeasonalListViewModel
    private lateinit var adapter: SeasonalListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSeasonalListBinding.inflate(inflater, container, false)

        // Initialize the viewmodel
        val application = requireNotNull(this.activity).application
        viewModel = TabSeasonalListViewModel(binding, application)

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
        //viewModel.columns.observe(viewLifecycleOwner) { columns ->


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

            // Hide toolbar when scrolling?
//            @SuppressLint("RestrictedApi")
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                if (dy > 0) {
////                    toolbar.hide()
//
//                } else if (dy < 0) {
////                    toolbar.show()
//                }
//            }
        }
        binding.seasonalListRV.addOnScrollListener(scrollListener)

        // Swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        setToolbar()

        return binding.root
    }

    private fun setToolbar() {
        // TODO: Season: Year
        binding.toolbar.title = viewModel.getSeasonYear()
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(true)
    }

    override fun onRefresh() {
        Timber.d("Refreshing...")
        viewModel.refreshList()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onResume() {
        setToolbar()

        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_seasonal, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.next_season -> { viewModel.nextSeason() ; setToolbar() ; true }
            R.id.prev_season -> { viewModel.prevSeason() ; setToolbar() ; true }
            R.id.now ->         { viewModel.currentSeason() ; setToolbar() ; true }
            R.id.span_size ->   { sliderDialog() ; true }
            else -> super.onOptionsItemSelected(item)
        }
    }


    // Select view mode
    private var columns = 2
    private fun sliderDialog() {
        val singleItems = arrayOf("1", "2", "3", "4", "5", "6", "7", "8")

        MaterialAlertDialogBuilder(requireContext(),
            com.google.android.material.R.style.Base_ThemeOverlay_AppCompat_Dialog)
            .setTitle("Number of columns to display")
            .setPositiveButton("CONFIRM", null)
            .setSingleChoiceItems(singleItems, columns-1) { item, which ->
                binding.seasonalListRV.layoutManager = GridLayoutManager(requireContext(), which+1)
                columns = which+1
            }
            .show()
    }
}
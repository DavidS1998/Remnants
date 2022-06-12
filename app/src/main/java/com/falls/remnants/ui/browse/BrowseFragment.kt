package com.falls.remnants.ui.browse

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.falls.remnants.R
import com.falls.remnants.adapter.AdapterTabPager
import com.falls.remnants.adapter.MediaViewType
import com.falls.remnants.data.Configs
import com.falls.remnants.databinding.FragmentBrowseBinding
import com.falls.remnants.ui.SeasonDialogFragment
import com.falls.remnants.ui.SliderDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber


class BrowseFragment : Fragment() {

    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var pageAdapter: AdapterTabPager
    private val viewModel: BrowseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBrowseBinding.inflate(inflater, container, false)

        // Fade out transition
        val transition = TransitionInflater.from(requireContext())
        exitTransition = transition.inflateTransition(R.transition.fade)

        // Create viewpager
        pageAdapter = AdapterTabPager(activity as FragmentActivity)
        viewPager = binding.viewpager
        viewPager.apply {
            adapter = pageAdapter
        }
        viewPager.adapter = pageAdapter

        // Update the toolbar to reflect the page
        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    setToolbar()
                }
            }
        )

        // Listen to needsRefresh
        viewModel.needsRefresh.observe(viewLifecycleOwner) {
            if (it) {
                binding.toolbar.title = viewModel.getSeasonYear()
                viewModel.refreshList(MediaViewType.SEASONAL)
                viewModel.needsRefresh.value = false
            }
        }

        // Initialize and add tab fragments
        pageAdapter.addFragment(TabSeasonalFragment())
        pageAdapter.addFragment(TabUpcomingFragment())
        pageAdapter.addFragment(TabSearchFragment())

        // TabLayout
        val tabs = binding.tabsLayout
        TabLayoutMediator(tabs, binding.viewpager) { tab, position ->
            tab.text = when (position) {
                // TODO: Programmatically set the tab text from page adapter
                0 -> "Seasonal"
                1 -> "Upcoming"
                2 -> "Search"
                else -> "What"
            }
        }.attach()

        return binding.root
    }

    // Set unique toolbars for each tab
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (viewPager.currentItem) {
            0 -> {
                inflater.inflate(R.menu.action_seasonal, menu)
                inflateSearch(menu)
            }
            1, 2 -> {
                inflater.inflate(R.menu.action_search, menu)
                inflateSearch(menu)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Toolbar menu actions
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.next_season -> {
                viewModel.nextSeason()
                binding.toolbar.title = viewModel.getSeasonYear()
                true
            }
            R.id.prev_season -> {
                viewModel.prevSeason()
                binding.toolbar.title = viewModel.getSeasonYear()
                true
            }
            R.id.now -> {
                viewModel.currentSeason()
                binding.toolbar.title = viewModel.getSeasonYear()
                true
            }
            R.id.span_size -> {
                sliderDialog(); true
            }
            R.id.select_season -> {
                selectSeasonDialog(); true
            }
            R.id.search -> {
                item.expandActionView()

                // Show keyboard
                (item.actionView as SearchView).requestFocus()
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(item.actionView, InputMethodManager.SHOW_IMPLICIT)
                true
            }
            R.id.toggle_user_list -> {
                viewModel.toggleShowOnlyUserAnime()
                true
            }
            R.id.toggle_dubs -> {
                viewModel.toggleShowOnlyDubs()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun setToolbar() {
        when (viewPager.currentItem) {
            0 -> {
                (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
                binding.toolbar.title = viewModel.getSeasonYear()
                setHasOptionsMenu(true)
            }
            1 -> {
                (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
                binding.toolbar.title = "UPCOMING"
                setHasOptionsMenu(true)
            }
            2 -> {
                (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
                binding.toolbar.title = viewModel.lastQuery
                setHasOptionsMenu(true)
            }
        }
    }

    private fun inflateSearch(menu: Menu) {
        val menuItem = menu.findItem(R.id.search)

        val searchView = menuItem.actionView as SearchView
        searchView.isIconifiedByDefault = false
        searchView.queryHint = "Search AniList"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    // Scroll to Search tab
                    viewPager.setCurrentItem(2, true)

                    // Execute search
                    viewModel.tempSearch(query)

                    // Set title
                    viewModel.lastQuery = "\"" + query.uppercase() + "\""
                    binding.toolbar.title = "\"" + query.uppercase() + "\""

                    // Hide keyboard
                    val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                // Procedural search
                if (viewPager.currentItem != 2) return false
                if (query != null) {
                    viewModel.tempSearch(query)
                    viewPager.setCurrentItem(2, true)

                    // Set title
                    viewModel.lastQuery = "\"" + query.uppercase() + "\""
                    binding.toolbar.title = "\"" + query.uppercase() + "\""
                    return true
                }
                return false
            }
        })
    }

    override fun onResume() {
        setToolbar()
        super.onResume()
    }

    private fun sliderDialog() {
        SliderDialogFragment().show(requireActivity().supportFragmentManager, "grid_size")
    }

    private fun selectSeasonDialog() {
        SeasonDialogFragment(viewModel).show(requireActivity().supportFragmentManager, "select_season")
    }
}
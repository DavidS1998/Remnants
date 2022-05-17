package com.falls.remnants.ui.browse

import android.app.Activity
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.falls.remnants.R
import com.falls.remnants.adapter.AdapterTabPager
import com.falls.remnants.data.Utils
import com.falls.remnants.databinding.FragmentBrowseBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator


class BrowseFragment : Fragment() {

    private var _binding: FragmentBrowseBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var pageAdapter: AdapterTabPager
    private val viewModel: BrowseViewModel by activityViewModels()

    override fun onAttach(activity: Activity) {
        // Apply settings
        val value = Utils.getSharedSettings(requireActivity(), "columns")
        viewModel.columns.value = value.toIntOrNull() ?: 1

        super.onAttach(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBrowseBinding.inflate(inflater, container, false)

        // Retrieve settings from shared preferences
//        val value = Utils.getSharedSettings(requireActivity(), "columns")
//        viewModel.columns.value = value.toIntOrNull() ?: 1

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

//      Initialize and add tab fragments
        val tab1 = TabSeasonalFragment()
        pageAdapter.addFragment(tab1)
        val tab2 = TabUpcomingFragment()
        pageAdapter.addFragment(tab2)
        val tab3 = TabTopFragment()
        pageAdapter.addFragment(tab3)

        // TabLayout
        val tabs = binding.tabsLayout
        TabLayoutMediator(tabs, binding.viewpager) { tab, position ->
            tab.text = when (position) {
                // TODO: Programmatically set the tab text from page adapter
                0 -> "Seasonal"
                1 -> "Upcoming"
                2 -> "Top rated"
                else -> "What"
            }
        }.attach()

        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (viewPager.currentItem) {
            0 -> inflater.inflate(R.menu.action_seasonal, menu)
            1 -> inflater.inflate(R.menu.action_generic_list, menu)
            2 -> inflater.inflate(R.menu.action_generic_list, menu)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        setToolbar()
        super.onResume()
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
                binding.toolbar.title = "TOP RATED"
                setHasOptionsMenu(true)
            }
        }
    }

    // TODO: Set default based on resolution
    // Select view mode
    private fun sliderDialog() {
        val singleItems = arrayOf("1", "2", "3", "4", "5", "6", "7", "8")
//
        MaterialAlertDialogBuilder(
            requireContext(),
            com.google.android.material.R.style.Base_ThemeOverlay_AppCompat_Dialog
        )
            .setTitle("Number of columns to display")
            .setPositiveButton("CONFIRM", null)
            .setSingleChoiceItems(singleItems, viewModel.columns.value ?: 0) { item, which ->
                viewModel.columns.value = which

                // Save column value to shared preferences
                Utils.saveSharedSettings(requireActivity(), "columns", which.toString())
            }
            .show()
    }
}
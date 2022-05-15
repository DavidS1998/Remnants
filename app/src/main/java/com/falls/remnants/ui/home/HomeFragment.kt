package com.falls.remnants.ui.home

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.falls.remnants.R
import com.falls.remnants.databinding.FragmentHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var pageAdapter: AdapterTabPager
    private val viewModel: TabSeasonalListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

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
        val tab1 = TabSeasonalListFragment()
        pageAdapter.addFragment(tab1, "Popular")
        val tab2 = TabTopFragment()
        pageAdapter.addFragment(tab2, "Top")

        // TabLayout
        val tabs = binding.tabsLayout
        TabLayoutMediator(tabs, binding.viewpager) { tab, position ->
            tab.text = when (position) {
                // TODO: Programmatically set the tab text from page adapter
                0 -> "Popular by season"
                1 -> "Top rated"
                else -> "What"
            }
        }.attach()

        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (viewPager.currentItem) {
            0 -> inflater.inflate(R.menu.action_seasonal, menu)
//            1 -> inflater.inflate(R.menu.action_seasonal, menu)
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
            .setSingleChoiceItems(singleItems, viewModel.columns.value?: 0 ) { item, which ->
                viewModel.columns.value = which
            }
            .show()
    }







    // ViewPager class for the tab fragments
    class AdapterTabPager(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()
        private val hashMap: HashMap<Int, Fragment> = HashMap()


        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getItemCount(): Int {
            return mFragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            val fragment: Fragment = mFragmentList[position]
            hashMap[position] = fragment
            return fragment
        }
    }
}
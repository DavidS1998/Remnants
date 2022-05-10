package com.falls.remnants.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.falls.remnants.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Create viewpager
        val pageAdapter = AdapterTabPager(activity as FragmentActivity)
        val viewPager: ViewPager2 = binding.viewpager
        viewPager.apply {
            adapter = pageAdapter
        }
        viewPager.adapter = pageAdapter

        // Initialize and add tab fragments
        val tab1 = TabSeasonalListFragment()
        pageAdapter.addFragment(tab1, "Tab 1")
        val tab2 = TabTopFragment()
        pageAdapter.addFragment(tab2, "Tab 2")

        // TabLayout
        val tabs = binding.tabsLayout
        TabLayoutMediator(tabs, binding.viewpager) { tab, position ->
            tab.text = when (position) {
                // TODO: Programmatically set the tab text from page adapter
                0 -> "Popular this season"
                1 -> "Top rated"
                else -> "What"
            }
        }.attach()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ViewPager class for the tab fragments
class AdapterTabPager(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val mFragmentList: MutableList<Fragment> = ArrayList()
    private val mFragmentTitleList: MutableList<String> = ArrayList()

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }
}
package com.falls.remnants.ui.library

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.falls.remnants.data.Utils
import com.falls.remnants.databinding.FragmentLibraryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var pageAdapter: AdapterTabPager
    private val viewModel: LibraryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLibraryBinding.inflate(inflater, container, false)

        // Fade out transition
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)

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
        pageAdapter.addFragment(TabCurrentFragment())
        pageAdapter.addFragment(TabUserListFragment())

        // TabLayout
        val tabs = binding.tabsLayout
        TabLayoutMediator(tabs, binding.viewpager) { tab, position ->
            tab.text = when (position) {
                // TODO: Programmatically set the tab text from page adapter
                0 -> "Current"
                1 -> "Library"
                else -> "What"
            }
        }.attach()

        // Spinner
        val spinnerArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item_button,
            resources.getStringArray(R.array.MediaLists)
        )
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.spinner.adapter = spinnerArrayAdapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.getMedia(MediaViewType.SEARCH)
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Update the view model
                if (viewModel._currentList == position) {
                    return
                }
                Timber.d("onItemSelected: $position")
                viewModel.listChanged()
                viewModel.getMedia(MediaViewType.SEARCH, position)
            }
        }

        // Observe logged in status
        Configs.loggedIn.observe(viewLifecycleOwner) {
            checkLoggedIn()
        }

        return binding.root
    }

    private fun checkLoggedIn() {
        if (Configs.loggedIn.value == false) {
            viewModel.refreshList(MediaViewType.SEARCH)
            viewModel.refreshList(MediaViewType.SEASONAL)
            binding.loggedInIndicator.visibility = View.VISIBLE
        } else {
            binding.loggedInIndicator.visibility = View.GONE
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (viewPager.currentItem) {
            0, 1 -> inflater.inflate(R.menu.action_columns_only, menu)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    // Toolbar menu actions
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
                binding.toolbar.title = "ACTIVE & AIRING"
                binding.spinner.visibility = View.GONE
                setHasOptionsMenu(true)
            }
            1 -> {
                (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
                binding.toolbar.title = ""
                binding.spinner.visibility = View.VISIBLE
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
            .setSingleChoiceItems(singleItems, Configs.columns.value ?: 0) { item, which ->

                // Save column value to shared preferences
                Configs.columns.value = which
                Utils.saveSharedSettings(requireActivity(), "columns", which.toString())

            }
            .show()
    }
}
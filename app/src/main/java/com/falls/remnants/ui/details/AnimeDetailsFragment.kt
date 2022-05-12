package com.falls.remnants.ui.details

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.falls.remnants.R
import com.falls.remnants.data.Anime
import com.falls.remnants.databinding.FragmentAnimeDetailsBinding
import com.falls.remnants.recycler.SeasonalListAdapter
import com.falls.remnants.ui.home.TabSeasonalListViewModel
import timber.log.Timber

class AnimeDetailsFragment : Fragment() {

    private var _binding: FragmentAnimeDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TabSeasonalListViewModel
    private lateinit var adapter: SeasonalListAdapter

    private lateinit var anime: Anime

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnimeDetailsBinding.inflate(inflater, container, false)

        anime = AnimeDetailsFragmentArgs.fromBundle(requireArguments()).anime
        binding.anime = anime


        // Initialize the viewmodel
//        val application = requireNotNull(this.activity).application
//        viewModel = AnimeDetailsViewModel(binding, application)

        // Toolbar
        setToolbar()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setToolbar()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarDetails)
        binding.toolbarDetails.title = anime.engTitle
        val toolbar = (activity as AppCompatActivity).supportActionBar
        toolbar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(false)
    }
}
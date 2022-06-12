package com.falls.remnants.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.falls.remnants.R
import com.falls.remnants.adapter.AdapterClickListener
import com.falls.remnants.adapter.MediaListAdapter
import com.falls.remnants.adapter.MediaViewType
import com.falls.remnants.data.Anime
import com.falls.remnants.data.Configs
import com.falls.remnants.databinding.FragmentAnimeDetailsBinding
import com.falls.remnants.ui.EditDialogFragment
import com.falls.remnants.ui.SliderDialogFragment
import com.falls.remnants.ui.library.LibraryFragmentDirections
import timber.log.Timber


class AnimeDetailsFragment : Fragment() {

    private var _binding: FragmentAnimeDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AnimeDetailsViewModel
    private lateinit var adapter: MediaListAdapter

    private lateinit var anime: Anime

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimeDetailsBinding.inflate(inflater, container, false)

        anime = AnimeDetailsFragmentArgs.fromBundle(requireArguments()).anime
        binding.anime = anime

        val transition = TransitionInflater.from(requireContext())
        enterTransition = transition.inflateTransition(R.transition.slide_up)

        // Set up the toolbar
        binding.toolbar.title = anime.engTitle
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(false)

        // Initialize the viewmodel
        val application = requireNotNull(this.activity).application
        viewModel = AnimeDetailsViewModel(binding, application)

        // Get detailed data
        viewModel.anime.observe(viewLifecycleOwner) { anime ->
            binding.anime = anime
            binding.descriptionText.text = Html.fromHtml(anime.description);
            if (anime.nextEpisode == "") {
                binding.status.visibility = View.GONE
            } else {
                binding.status.visibility = View.VISIBLE
            }
        }
        viewModel.getAnimeDetails(anime.id)

        // Hide personal stats if not logged in
        if (Configs.loggedIn.value == true) {
            binding.user.visibility = View.VISIBLE
        } else {
            binding.user.visibility = View.GONE
        }

        // Refresh observer
        viewModel.needsRefresh.observe(viewLifecycleOwner) { needsRefresh ->
            if (needsRefresh) {
                viewModel.getAnimeDetails(anime.id)
                viewModel.needsRefresh.value = false
            }
        }

        // Recycler view adapter
        adapter = MediaListAdapter(
            AdapterClickListener {
                val action =
                    AnimeDetailsFragmentDirections.actionAnimeDetailsFragmentSelf(it)
                findNavController().navigate(action)
            }, MediaViewType.RELATED
        )
        binding.recyclerView.adapter = adapter
        viewModel.relatedAnime.observe(viewLifecycleOwner) { anime ->
            anime?.let {
                adapter.submitList(anime)
                binding.recyclerView.scrollToPosition(0)
            }
        }

        // AniList button
        binding.anilistButton.setOnClickListener {
            val imdbIntent =
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://anilist.co/anime/" + binding.anime?.id)
                )
            startActivity(imdbIntent)
        }

        // MAL button
        binding.malButton.setOnClickListener {
            val imdbIntent =
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://myanimelist.net/anime/" + binding.anime?.idMAL)
                )
            startActivity(imdbIntent)
        }

        // Edit button
        binding.editButton.setOnClickListener {
            editDialog()
        }

        return binding.root
    }

    private fun editDialog() {
        EditDialogFragment(binding.anime ?: anime, viewModel).show(requireActivity().supportFragmentManager, "edit_entry")
    }
}

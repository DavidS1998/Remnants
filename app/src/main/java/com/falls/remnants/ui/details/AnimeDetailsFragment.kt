package com.falls.remnants.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.falls.remnants.data.Anime
import com.falls.remnants.databinding.FragmentAnimeDetailsBinding
import timber.log.Timber


class AnimeDetailsFragment : Fragment() {

    private var _binding: FragmentAnimeDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AnimeDetailsViewModel

    private lateinit var anime: Anime

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimeDetailsBinding.inflate(inflater, container, false)

        anime = AnimeDetailsFragmentArgs.fromBundle(requireArguments()).anime
        binding.anime = anime

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

        // AniList button
        binding.anilistButton.setOnClickListener {
            val imdbIntent =
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://anilist.co/anime/" + binding.anime?.id)
                )
            startActivity(imdbIntent)
        }
        return binding.root
    }
}

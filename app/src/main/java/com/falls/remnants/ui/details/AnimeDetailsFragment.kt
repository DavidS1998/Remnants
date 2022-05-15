package com.falls.remnants.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.fragment.app.Fragment
import com.falls.remnants.data.Anime
import com.falls.remnants.databinding.FragmentAnimeDetailsBinding
import timber.log.Timber


class AnimeDetailsFragment : Fragment() {

    private var _binding: FragmentAnimeDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AnimeDetailsViewModel
//    private lateinit var adapter: SeasonalListAdapter

    private lateinit var anime: Anime

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnimeDetailsBinding.inflate(inflater, container, false)

        anime = AnimeDetailsFragmentArgs.fromBundle(requireArguments()).anime
        binding.anime = anime

        // Initialize the viewmodel
        val application = requireNotNull(this.activity).application
        viewModel = AnimeDetailsViewModel(binding, application)

        // Get detailed data
        viewModel.anime.observe(viewLifecycleOwner) { anime ->
            binding.anime = anime
            binding.descriptionText.setText(Html.fromHtml(anime.description));
        }
        viewModel.getAnimeDetails(anime.id)

        binding.anilistButton.setOnClickListener {
            val imdbIntent: Intent =
                Uri.parse("https://anilist.co/anime/" + binding.anime?.id).let { url ->
                    Intent(Intent.ACTION_VIEW, url)
                }
            startActivity(imdbIntent)
        }
        return binding.root
    }
}

package com.falls.remnants.ui

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo3.api.Optional
import com.falls.remnants.R
import com.falls.remnants.UpdateQueryMutation
import com.falls.remnants.data.AnilistQueries.updateAnime
import com.falls.remnants.data.Anime
import com.falls.remnants.data.Configs
import com.falls.remnants.data.Utils
import com.falls.remnants.databinding.DialogColumnsBinding
import com.falls.remnants.databinding.DialogEditBinding
import com.falls.remnants.databinding.DialogSelectSeasonBinding
import com.falls.remnants.networking.GraphQLapi
import com.falls.remnants.type.MediaListStatus
import com.falls.remnants.type.MediaSeason
import com.falls.remnants.ui.browse.BrowseViewModel
import com.falls.remnants.ui.details.AnimeDetailsViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

// TODO: Set default based on resolution
class SliderDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = DialogColumnsBinding.inflate(layoutInflater, null, false)
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
        dialog.setView(binding.root)
        dialog.setPositiveButton("Confirm") { _, _ -> }

        binding.seekBarText.text = "Columns: " + Configs.columns.value.toString()
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.seekBarText.text = "Columns: " + progress.toString()
                Configs.columns.value = progress
                Utils.saveSharedSettings(requireActivity(), "columns", progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.seekBar.progress = Configs.columns.value?.toInt() ?: 2

        return dialog.create()
    }
}

class SeasonDialogFragment(viewModel: BrowseViewModel) : DialogFragment() {

    private val viewModel: BrowseViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Default date
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)

        val binding = DialogSelectSeasonBinding.inflate(layoutInflater, null, false)
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
        dialog.setView(binding.root)
        dialog.setPositiveButton("Confirm") { _, _ ->
            when (binding.chipGroupSeason.checkedChipId) {
                R.id.chip_winter -> viewModel.setSeason = MediaSeason.WINTER
                R.id.chip_spring -> viewModel.setSeason = MediaSeason.SPRING
                R.id.chip_summer -> viewModel.setSeason = MediaSeason.SUMMER
                R.id.chip_fall -> viewModel.setSeason = MediaSeason.FALL
            }
            viewModel.year = binding.yearPicker.value

            viewModel.needsRefresh.value = true
        }

        binding.yearPicker.minValue = year - 100
        binding.yearPicker.maxValue = year + 10
        binding.yearPicker.value = viewModel.year

        // Check default chip based on set season
        when (viewModel.setSeason) {
            MediaSeason.SPRING -> binding.chipSpring.isChecked = true
            MediaSeason.SUMMER -> binding.chipSummer.isChecked = true
            MediaSeason.FALL -> binding.chipFall.isChecked = true
            MediaSeason.WINTER -> binding.chipWinter.isChecked = true
            else -> {
                binding.chipSpring.isChecked = false
                binding.chipSummer.isChecked = false
                binding.chipFall.isChecked = false
                binding.chipWinter.isChecked = false
            }
        }


//        // Year listener
//        binding.yearPicker.setOnValueChangedListener { _, _, newVal ->
//            viewModel.year = newVal
//            viewModel.needsRefresh.value = true
//        }
//
//        // Season listener
//        binding.chipGroupSeason.setOnCheckedChangeListener { _, checkedId ->
//            when (checkedId) {
//                R.id.chip_spring -> viewModel.setSeason = MediaSeason.SPRING
//                R.id.chip_summer -> viewModel.setSeason = MediaSeason.SUMMER
//                R.id.chip_fall -> viewModel.setSeason = MediaSeason.FALL
//                R.id.chip_winter -> viewModel.setSeason = MediaSeason.WINTER
//            }
//            viewModel.needsRefresh.value = true
//        }

        return dialog.create()
    }

}

class EditDialogFragment(var anime: Anime, val viewModel: AnimeDetailsViewModel) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = DialogEditBinding.inflate(layoutInflater, null, false)
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
        dialog.setView(binding.root)
        dialog.setNegativeButton("Cancel") { _, _ -> }
        dialog.setPositiveButton("Edit") { _, _ ->
            // Convert seasonSpinner to MediaListStatus
            val status = when (binding.seasonSpinner.selectedItem.toString()) {
                "CURRENT" -> MediaListStatus.CURRENT
                "PLANNING" -> MediaListStatus.PLANNING
                "COMPLETED" -> MediaListStatus.COMPLETED
                "DROPPED" -> MediaListStatus.DROPPED
                "PAUSED" -> MediaListStatus.PAUSED
                "REPEATING" -> MediaListStatus.REPEATING
                else -> null
            }

            lifecycleScope.launch {
                updateAnime(
                    anime.id,
                    status,
                    binding.scoreTracker.text.toString().toDoubleOrNull()?.div(10),
                    binding.episodeTracker.text.toString().toIntOrNull())
            }

            viewModel.needsRefresh.value = true
            Toast.makeText(requireContext(), "Update successful", Toast.LENGTH_SHORT).show()
        }

        // Spinner
        val spinnerArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item_button,
            resources.getStringArray(R.array.MediaLists).drop(1)
        )
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.seasonSpinner.adapter = spinnerArrayAdapter

        // Defaults
        binding.episodeTracker.hint = anime.userProgress
        binding.scoreTracker.hint = anime.userScore
        Timber.d(anime.userStatus)
        binding.seasonSpinner.setSelection(when (anime.userStatus) {
            "CURRENT" -> 0
            "COMPLETED" -> 1
            "PAUSED" -> 2
            "DROPPED" -> 3
            "REPEATING" -> 4
            "PLANNING" -> 5
            else -> 0 // Default for non-listed anime
        })

        return dialog.create()
    }
}
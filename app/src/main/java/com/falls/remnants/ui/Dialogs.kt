package com.falls.remnants.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.falls.remnants.R
import com.falls.remnants.data.Configs
import com.falls.remnants.data.Utils
import com.falls.remnants.databinding.DialogColumnsBinding
import com.falls.remnants.databinding.DialogSelectSeasonBinding
import com.falls.remnants.type.MediaSeason
import com.falls.remnants.ui.browse.BrowseViewModel
import java.util.*

// TODO: Set default based on resolution
class SliderDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = DialogColumnsBinding.inflate(layoutInflater, null, false)
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
        dialog.setView(binding.root)
        dialog.setPositiveButton("Confirm") { _, _ -> }

        binding.seekBarText.text = "Columns: " + Configs.columns.toString()
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
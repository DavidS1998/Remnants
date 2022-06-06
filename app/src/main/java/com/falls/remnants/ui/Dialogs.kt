package com.falls.remnants.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.falls.remnants.R
import com.falls.remnants.data.Configs
import com.falls.remnants.data.Utils
import com.falls.remnants.databinding.DialogColumnsBinding

// TODO: Set default based on resolution
class SliderDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = DialogColumnsBinding.inflate(layoutInflater, null, false)
        val dialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        dialog.setView(binding.root)
        dialog.setPositiveButton("Confirm") { _, _ -> }

        binding.seekBar.progress = Configs.columns.value?.toInt() ?: 2
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.seekBarText.text = "Columns: " + progress.toString()
                Configs.columns.value = progress
                Utils.saveSharedSettings(requireActivity(), "columns", progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return dialog.create()
    }
}

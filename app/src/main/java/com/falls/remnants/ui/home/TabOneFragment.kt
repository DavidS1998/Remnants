package com.falls.remnants.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.falls.remnants.databinding.FragmentTabOneBinding
import timber.log.Timber

public class TabOneFragment : Fragment() {

    private var _binding: FragmentTabOneBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTabOneBinding.inflate(inflater, container, false)
        binding.textHome.text = "Sup"

        return binding.root
    }
}
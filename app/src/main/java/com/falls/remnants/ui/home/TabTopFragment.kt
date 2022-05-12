package com.falls.remnants.ui.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.falls.remnants.databinding.FragmentSeasonalListBinding
import com.falls.remnants.databinding.FragmentTabTwoBinding

public class TabTopFragment : Fragment() {

    private var _binding: FragmentTabTwoBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater:LayoutInflater,
        container:ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTabTwoBinding.inflate(inflater, container, false)

        setToolbar()
        return binding.root
    }

    private fun setToolbar() {
        binding.toolbar.title = "Top"
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(false)
    }

    override fun onResume() {
        setToolbar()

        super.onResume()
    }

}

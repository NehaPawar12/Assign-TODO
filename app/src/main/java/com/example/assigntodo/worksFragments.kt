package com.example.assigntodo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.assigntodo.databinding.FragmentWorksFragmentsBinding


class worksFragments : Fragment() {

    private lateinit var binding: FragmentWorksFragmentsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWorksFragmentsBinding.inflate(layoutInflater)
        binding.fabAssignWork.setOnClickListener {
            findNavController().navigate(R.id.action_worksFragments_to_assignWorkFragment)
        }
        return binding.root
    }

}
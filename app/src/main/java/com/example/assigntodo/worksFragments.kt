package com.example.assigntodo

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assigntodo.auth.SignInActivity
import com.example.assigntodo.databinding.FragmentWorksFragmentsBinding
import com.example.assigntodo.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class worksFragments : Fragment() {

    val empData by navArgs<worksFragmentsArgs>()
    private lateinit var worksAdapter: WorksAdapter
    private lateinit var binding: FragmentWorksFragmentsBinding
    private lateinit var workRoom: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWorksFragmentsBinding.inflate(layoutInflater)
        val empDataToPass = empData.employeeData
        binding.fabAssignWork.setOnClickListener {
            val action = worksFragmentsDirections.actionWorksFragmentsToAssignWorkFragment(empDataToPass)
            findNavController().navigate(action)
        }

        val empName = empData.employeeData.userName

        binding.tbEmpWorks.apply {
            title = empName
            setNavigationIcon(R.drawable.baseline_arrow_back_24)
            setNavigationOnClickListener{
                activity?.onBackPressed()
            }
        }
        prepareRvForWorks()
        showAllWorks()
        return binding.root
    }

    private fun showAllWorks() {

        Utils.showDialog(requireContext())
        val bossId = FirebaseAuth.getInstance().currentUser?.uid
        workRoom = bossId + empData.employeeData.userId
        FirebaseDatabase.getInstance().getReference("Works").child(workRoom)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val workList = ArrayList<Works>()
                    for(allWorks in snapshot.children){
                        val work = allWorks.getValue(Works::class.java)
                        workList.add(work!!)
                    }
                    worksAdapter.differ.submitList(workList)
                    Utils.hideDialog()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun prepareRvForWorks() {
        worksAdapter = WorksAdapter(::onUnassignedButtonClicked)
        binding.rvWorks.apply {
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter = worksAdapter
        }
    }

    private fun onUnassignedButtonClicked(works: Works){
        val builder = AlertDialog.Builder(requireContext())
        val alertDialog = builder.create()
        builder.setTitle("Unassign Work")
            .setMessage("Are you sure you want to Unassign this work?")
            .setPositiveButton("Yes"){_,_->
                unAssignedWork(works)
            }
            .setNegativeButton("No"){_,_->
                alertDialog.dismiss()
            }
            .show()

    }

    private fun unAssignedWork(works: Works) {
        works.expanded = !works.expanded
        FirebaseDatabase.getInstance().getReference("Works").child(workRoom)
            .addValueEventListener(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(allWorks in snapshot.children){
                        val currentWork = allWorks.getValue(Works::class.java)
                        if(currentWork == works){
                            allWorks.ref.removeValue()
                            Utils.showToast(requireContext(),"Work has been Unassigned!!")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}
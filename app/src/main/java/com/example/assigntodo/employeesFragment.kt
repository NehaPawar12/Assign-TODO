package com.example.assigntodo

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assigntodo.auth.SignInActivity
import com.example.assigntodo.databinding.ActivitySignUpBinding
import com.example.assigntodo.databinding.FragmentEmployeesBinding
import com.example.assigntodo.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class employeesFragment : Fragment() {

    private lateinit var binding: FragmentEmployeesBinding
    private lateinit var employeesAdapter: EmployeesAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEmployeesBinding.inflate(layoutInflater)

        binding.apply {
            tbEmployees.setOnMenuItemClickListener{
                when(it.itemId){
                    R.id.logOut -> {
                        showLogOutDialog()
                        true
                    }

                    else -> false
                }

            }

        }

        prepareRVForEmployeeAdapter()
        showAllEmployees()

        return binding.root
    }

    private fun showAllEmployees(){
        Utils.showDialog(requireContext())
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val empList = arrayListOf<Users>()
                for(employees in snapshot.children){
                    val currentUser = employees.getValue(Users::class.java)
                    if(currentUser?.userType == "Employee"){
                        empList.add(currentUser)
                    }
                }
                employeesAdapter.differ.submitList(empList)
                Utils.hideDialog()
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.apply {
                    hideDialog()
                    showToast(requireContext(),error.message)
                }
            }
        })
    }
    private fun prepareRVForEmployeeAdapter(){
        employeesAdapter = EmployeesAdapter()
        binding.rvEmployees.apply {
            layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            adapter = employeesAdapter
        }
    }

    private fun showLogOutDialog(){
        val builder = AlertDialog.Builder(requireContext())
        val alertDialog = builder.create()
        builder.setTitle("Log Out")
            .setPositiveButton("Yes"){_,_->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(requireContext(),SignInActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("No"){_,_->
                alertDialog.dismiss()
            }
            .show()
            .setCancelable(false)
    }

}
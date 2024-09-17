package com.example.assigntodo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assigntodo.auth.SignInActivity
import com.example.assigntodo.databinding.ActivityEmployeeMainBinding
import com.example.assigntodo.utils.Utils
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EmployeeMainActivity() : AppCompatActivity() {

    var  employeeWorkAdapter = EmployeeWorkAdapter(
        ::onProgressButtonClicked,
        ::onCompletedButtonClicked
    )

    private lateinit var binding: ActivityEmployeeMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            tbEmployee.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.logOut -> {
                        showLogOutDialog()
                        true
                    }

                    else -> false
                }

            }
        }
        prepareRvForEmployeeWorksAdapter()
        showEmployeeWorks()

    }

    private fun prepareRvForEmployeeWorksAdapter(){
       employeeWorkAdapter = EmployeeWorkAdapter(::onProgressButtonClicked,::onCompletedButtonClicked)
        binding.rvEmployeeWork.apply {
            layoutManager = LinearLayoutManager(this@EmployeeMainActivity,
                LinearLayoutManager.VERTICAL,false)
            adapter = employeeWorkAdapter
        }
    }

    private fun showEmployeeWorks(){
        Utils.showDialog(this)
        val empId = FirebaseAuth.getInstance().currentUser?.uid
        val workRef = FirebaseDatabase.getInstance().getReference("Works")
            workRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(workRooms in snapshot.children)
                    {
                        if(workRooms.key?.contains(empId!!) == true){
                            val empWorkRef = workRef.child(workRooms.key!!)
                            empWorkRef.addValueEventListener(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val workList = ArrayList<Works>()
                                    for(works in snapshot.children){
                                        val work = works.getValue(Works::class.java)
                                        workList.add(work!!)
                                    }
                                    employeeWorkAdapter.differ.submitList(workList)
                                    Utils.hideDialog()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
    private fun showLogOutDialog(){
        val builder = AlertDialog.Builder(this)
        val alertDialog = builder.create()
        builder.setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes"){_,_->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, SignInActivity::class.java))
                this.finish()
            }
            .setNegativeButton("No"){_,_->
                alertDialog.dismiss()
            }
            .show()
            .setCancelable(false)
    }

    private fun onProgressButtonClicked(works: Works, progressButton : MaterialButton) {
        if (progressButton.text != "In Progress") {


            val builder = AlertDialog.Builder(this)
            val alertDialog = builder.create()
            builder.setTitle("Starting Work")
                .setMessage("Are you starting this work?")
                .setPositiveButton("Yes") { _, _ ->
                    progressButton.apply {
                        text = "In Progress"
                        setTextColor(
                            ContextCompat.getColor(
                                this@EmployeeMainActivity,
                                R.color.light5
                            )
                        )
                    }
                    updateStatus(works, "2")
                }
                .setNegativeButton("No") { _, _ ->
                    alertDialog.dismiss()
                }
                .show()
                .setCancelable(false)
        }
        else{
            Utils.showToast(this,"Work is in progress or completed")
        }
    }

    private fun updateStatus(works: Works, status: String) {
        val empId = FirebaseAuth.getInstance().currentUser?.uid
        val workRef = FirebaseDatabase.getInstance()
        workRef.getReference("Works").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(workRooms in snapshot.children){
                    if(workRooms.key?.contains(empId!!) == true){
                        val empWorkRef = workRef.getReference("Works").child(workRooms.key!!)
                        empWorkRef.addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {

                                for(allWorks in snapshot.children){
                                    val work = allWorks.getValue(Works::class.java)
                                    if(works.workId == work?.workId){
                                        empWorkRef.child(allWorks.key!!).child("workStatus").setValue(status)
                                    }
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                                Utils.hideDialog()
                            }

                        })

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun onCompletedButtonClicked(works: Works, completedButton : MaterialButton) {
        if (completedButton.text != "Work Completed") {
            val builder = AlertDialog.Builder(this)
            val alertDialog = builder.create()
            builder.setTitle("Work Completed")
                .setMessage("Have you completed this work?")
                .setPositiveButton("Yes") { _, _ ->
                    completedButton.apply {
                        text = "Completed"
                        setTextColor(
                            ContextCompat.getColor(
                                this@EmployeeMainActivity,
                                R.color.light5
                            )
                        )
                    }
                    updateStatus(works, "3")
                }
                .setNegativeButton("No") { _, _ ->
                    alertDialog.dismiss()
                }
                .show()
                .setCancelable(false)
        }
        else{
            Utils.showToast(this,"Work is in progress or completed")
        }
    }
}
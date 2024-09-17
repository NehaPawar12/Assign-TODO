package com.example.assigntodo

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.assigntodo.databinding.FragmentAssignWorkBinding
import com.example.assigntodo.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale


class assignWorkFragment : Fragment() {
    val employeeData by navArgs<assignWorkFragmentArgs>()
   private lateinit var binding: FragmentAssignWorkBinding
    private var priority = "1"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAssignWorkBinding.inflate(layoutInflater)

        binding.tbAssignWork.apply {
            setNavigationOnClickListener{
                activity?.onBackPressed()
            }
        }

        setPriority()
        setDate()
        binding.btnDone.setOnClickListener {
            assignWork()
        }
        return binding.root
    }

    private fun assignWork(){
        Utils.showDialog(requireContext())

        val workTitle = binding.etTitle.text.toString()
        val workDescription = binding.etWorkDesc.text.toString()
        val workLastDate = binding.tvDate.text.toString()

        if(workTitle.isEmpty()){
            Utils.apply {
                showToast(requireContext(),"Please select work title")
            }
        }
        else if(workLastDate == "Last Date : "){
            Utils.apply {
                hideDialog()
                showToast(requireContext(),"Please select last date")
            }
        }
        else{
            val empId = employeeData.employeeDetail.userId
            val bossId = FirebaseAuth.getInstance().currentUser?.uid
            val workRoom = bossId + empId
            val randomId = (1 .. 20).map {(('A' .. 'Z') + ('a' .. 'z')+ ('0' .. '9')).random()}.joinToString("")

            val work = Works(
                workId = randomId,
                workTitle = workTitle,
                workDesc = workDescription,
                workPriority = priority,
                workLastDate = workLastDate,
                workStatus = "1"

            )
            FirebaseDatabase.getInstance().getReference("Works").child(workRoom).child(randomId)
                .setValue(work)
                .addOnSuccessListener {
                    Utils.hideDialog()
                    Utils.showToast(
                        requireContext(),
                        "Work Assigned to ${employeeData.employeeDetail.userName}")
                    val action =
                        assignWorkFragmentDirections.actionAssignWorkFragmentToWorksFragments(
                            employeeData.employeeDetail
                        )
                    findNavController().navigate(action)

                }
        }

    }

    private fun setDate(){
        val myCalender = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener{
            view,year,month, dayOfMonth ->
            myCalender.apply {
                set(Calendar.YEAR,year)
                set(Calendar.MONTH,month)
                set(Calendar.DAY_OF_MONTH,dayOfMonth)
                updateLable(myCalender)
            }
        }
        binding.greenOval.setImageResource(R.drawable.tickmark)
        binding.datePicker.setOnClickListener {
            DatePickerDialog(requireContext(),datePicker,myCalender.get(Calendar.YEAR),myCalender.get(Calendar.MONTH),myCalender.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateLable(myCalendar: Calendar) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        binding.tvDate.text = sdf.format(myCalendar.time)
    }

    private fun setPriority(){
        binding.apply {
            greenOval.setOnClickListener{
                Utils.showToast(requireContext(), "Priority : Low")
                priority= "1"
                binding.greenOval.setImageResource(R.drawable.tickmark)
                binding.yellowOval.setImageResource(0)
                binding.redOval.setImageResource(0)
            }

            yellowOval.setOnClickListener{
                Utils.showToast(requireContext(), "Priority : Medium")
                priority= "2"
                binding.yellowOval.setImageResource(R.drawable.tickmark)
                binding.greenOval.setImageResource(0)
                binding.redOval.setImageResource(0)
            }

            redOval.setOnClickListener{
                Utils.showToast(requireContext(), "Priority : High")
                priority= "3"
                binding.redOval.setImageResource(R.drawable.tickmark)
                binding.yellowOval.setImageResource(0)
                binding.greenOval.setImageResource(0)
            }

        }
    }

}
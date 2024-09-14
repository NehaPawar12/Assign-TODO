package com.example.assigntodo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.assigntodo.databinding.ActivityEmployeeMainBinding
import com.example.assigntodo.databinding.ItemViewEmployeesProfileBinding

class EmployeesAdapter : RecyclerView.Adapter<EmployeesAdapter.EmployeeViewHolder>() {
    class EmployeeViewHolder(val binding: ItemViewEmployeesProfileBinding) :
        RecyclerView.ViewHolder(binding.root)

    val diffUtil = object : DiffUtil.ItemCallback<Users>() {
        override fun areItemsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        return EmployeeViewHolder(
            ItemViewEmployeesProfileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val empData = differ.currentList[position]
        holder.binding.apply {
            Glide.with(holder.itemView).load(empData.userImage).into(ivEmployeeProfile)
            tvEmployeeName.text = empData.userName

        }
        holder.itemView.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_employeesFragment_to_worksFragments)
        }
    }
}
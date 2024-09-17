package com.example.assigntodo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.assigntodo.databinding.ItemViewWorksFragmentBinding

class WorksAdapter(val onUnassignedButtonClicked: (Works) -> Unit) : RecyclerView.Adapter<WorksAdapter.WorksViewHolder>() {
    class WorksViewHolder(val binding: ItemViewWorksFragmentBinding) :
        RecyclerView.ViewHolder(binding.root)

    val diffUtil = object : DiffUtil.ItemCallback<Works>() {
        override fun areItemsTheSame(oldItem: Works, newItem: Works): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Works, newItem: Works): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorksViewHolder{
        return WorksViewHolder(
            ItemViewWorksFragmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: WorksViewHolder, position: Int) {
        val works = differ.currentList[position]
        val isExpanded = works.expanded
        holder.binding.apply {
            tvTitle.text = works.workTitle
            tvDate.text = works.workLastDate
            tvWorkDescrition.text = works.workDesc

            when(works.workPriority){
                "1" -> ivOval.setImageResource(R.drawable.greenoval)
                "2" -> ivOval.setImageResource(R.drawable.yellowoval)
                "3" -> ivOval.setImageResource(R.drawable.redoval)
            }
            when(works.workStatus){
                "1" -> holder.binding.tvStatus.text = "Pending"
                "2" -> holder.binding.tvStatus.text = "In Progress"
                "3" -> holder.binding.tvStatus.text = "Completed"
            }
            tvWorkDescrition.visibility = if (isExpanded) View.VISIBLE else View.GONE
            workDescT.visibility = if (isExpanded) View.VISIBLE else View.GONE
            btnWorkDone.visibility = if (isExpanded) View.VISIBLE else View.GONE

            constraintLayout.setOnClickListener {
                isAnyItemExpanded(position)
                works.expanded = !works.expanded
                notifyItemChanged(position,0)
            }
            btnWorkDone.setOnClickListener {
                onUnassignedButtonClicked(works)
            }
        }



    }
    private fun isAnyItemExpanded(position: Int) {
        val expandedItemIndex = differ.currentList.indexOfFirst { it.expanded }
        if(expandedItemIndex>=0 && expandedItemIndex!=position){
            differ.currentList[expandedItemIndex].expanded = false
            notifyItemChanged(expandedItemIndex,0)
        }
    }

    override fun onBindViewHolder(
        holder: WorksViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if(payloads.isNotEmpty()&& payloads[0] == 0){
            holder.binding.tvWorkDescrition.visibility = View.GONE
            holder.binding.workDescT.visibility = View.GONE
            holder.binding.btnWorkDone.visibility = View.GONE
        }
        super.onBindViewHolder(holder, position, payloads)
    }
}
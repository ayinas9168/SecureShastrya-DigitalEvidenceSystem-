package com.example.secureshastrya.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.secureshastrya.EvidenceViewerActivity
import com.example.secureshastrya.data.Evidence
import com.example.secureshastrya.databinding.ItemEvidenceBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EvidenceAdapter : ListAdapter<Evidence, EvidenceAdapter.EvidenceViewHolder>(DiffCallback()) {

    var isSelectionMode = false
        set(value) {
            field = value
            selectedItems.clear()
            notifyDataSetChanged()
        }

    val selectedItems = mutableSetOf<Evidence>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvidenceViewHolder {
        val binding = ItemEvidenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EvidenceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EvidenceViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, isSelectionMode, selectedItems.contains(currentItem)) { isChecked ->
            if (isChecked) selectedItems.add(currentItem) else selectedItems.remove(currentItem)
        }
    }

    class EvidenceViewHolder(private val binding: ItemEvidenceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(evidence: Evidence, isSelectionMode: Boolean, isSelected: Boolean, onSelectionChanged: (Boolean) -> Unit) {
            binding.evidence = evidence
            
            binding.cbSelected.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            binding.cbSelected.setOnCheckedChangeListener(null)
            binding.cbSelected.isChecked = isSelected
            binding.cbSelected.setOnCheckedChangeListener { _, isChecked -> onSelectionChanged(isChecked) }

            // Format timestamp for display
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val dateString = sdf.format(Date(evidence.timestamp))
            binding.tvTimestamp.text = dateString
            
            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    binding.cbSelected.isChecked = !binding.cbSelected.isChecked
                } else {
                    val context = it.context
                    val intent = Intent(context, EvidenceViewerActivity::class.java).apply {
                        putExtra("evidenceId", evidence.evidenceId)
                    }
                    context.startActivity(intent)
                }
            }
            
            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Evidence>() {
        override fun areItemsTheSame(oldItem: Evidence, newItem: Evidence) =
            oldItem.evidenceId == newItem.evidenceId

        override fun areContentsTheSame(oldItem: Evidence, newItem: Evidence) = oldItem == newItem
    }
}

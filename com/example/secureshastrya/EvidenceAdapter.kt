package com.example.secureshastrya

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.secureshastrya.data.Evidence
import com.example.secureshastrya.databinding.ItemEvidenceBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EvidenceAdapter : ListAdapter<Evidence, EvidenceAdapter.EvidenceViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvidenceViewHolder {
        val binding = ItemEvidenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EvidenceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EvidenceViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class EvidenceViewHolder(private val binding: ItemEvidenceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(evidence: Evidence) {
            binding.evidence = evidence
            
            // Format timestamp for display
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val dateString = sdf.format(Date(evidence.timestamp))
            binding.tvTimestamp.text = dateString
            
            binding.root.setOnClickListener {
                val context = it.context
                val intent = Intent(context, EvidenceViewerActivity::class.java).apply {
                    putExtra("evidenceId", evidence.evidenceId)
                }
                context.startActivity(intent)
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
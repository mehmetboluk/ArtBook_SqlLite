package com.example.artbook_sqllite

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.artbook_sqllite.databinding.RecycleRowLayoutBinding

class RecyclerAdaptor(val artList : ArrayList<Art>) : RecyclerView.Adapter<RecyclerAdaptor.ArtHolder>() {

    class ArtHolder(val binding : RecycleRowLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = RecycleRowLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)

    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.tvRecycleIcon.text = artList.get(position).name
        holder.itemView.setOnClickListener(){
            val intent = Intent(holder.itemView.context, DetailsActivity::class.java)
            intent.putExtra("info", "old")
            intent.putExtra("id", artList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return artList.size
    }
}
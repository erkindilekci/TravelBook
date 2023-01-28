package com.erkindilekci.travelbook

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.erkindilekci.travelbook.databinding.RecyclerRowBinding

class PlaceAdapter(val placeList: ArrayList<Place>) : RecyclerView.Adapter<PlaceAdapter.Holder>(){

    class Holder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.recyclerViewTextView.text = placeList.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("info", "old")
            intent.putExtra("id", placeList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return placeList.size
    }
}
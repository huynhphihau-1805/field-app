package com.crayon.fieldapp.ui.screen.report.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crayon.fieldapp.R

class SimpleRVAdapter constructor(
    val items: ArrayList<String>,
    val context: Context
) : RecyclerView.Adapter<SimpleRVAdapter.StoreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_simple, parent, false)
        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        val data = items[position]
        holder.txtName.text = data
    }

    inner class StoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtName: TextView

        init {
            txtName = itemView.findViewById(R.id.txt_name)
        }
    }

    fun addAll(listItems: ArrayList<String>) {
        items.clear()
        items.addAll(listItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return this.items.size
    }
}
package com.example.sos

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.sos.Room.ContactEntity

class CAdapter(private val context : Context, private val listener : Contact) : RecyclerView.Adapter<CAdapter.ContactViewHolder>() {

    companion object{
        val allContacts = ArrayList<ContactEntity>()
    }


    inner class ContactViewHolder(itemview : View) : ViewHolder(itemview){
        val t1: TextView = itemview.findViewById(R.id.item_name)
        val t2: TextView = itemview.findViewById(R.id.item_ph)
        val list: ConstraintLayout = itemview.findViewById(R.id.list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val viewHolder = ContactViewHolder(LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false))
        viewHolder.list.setOnLongClickListener {
            listener.onLongClickListener(allContacts[viewHolder.adapterPosition])
        }
        return viewHolder
    }

    override fun getItemCount(): Int {
        return allContacts.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currContact = allContacts[position]
        holder.t1.text = currContact.name
        holder.t2.text = currContact.phone
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList : List<ContactEntity>){
        allContacts.clear()
        allContacts.addAll(newList)
        notifyDataSetChanged()
    }

}

interface Contact{
    fun onLongClickListener(contactEntity: ContactEntity) : Boolean
}
package com.rain2002kr.android_tinderapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CardItemAdpter :ListAdapter<CardItem, CardItemAdpter.ViewHolder>(diffUtil){
    inner class ViewHolder(private val view: View): RecyclerView.ViewHolder(view){

        fun bind(cardItem: CardItem){
            view.findViewById<TextView>(R.id.nameTextView).text = cardItem.name

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardItemAdpter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_card, parent, false))
    }

    override fun onBindViewHolder(holder: CardItemAdpter.ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<CardItem>(){
            override fun areItemsTheSame(oldItem: CardItem, newItem: CardItem): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: CardItem, newItem: CardItem): Boolean {
                return oldItem == newItem
            }
        }
    }

}






package com.example.netpay.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netpay.R

class FriendAdapter(private val onSettleUp: (String) -> Unit) :
    ListAdapter<FriendBalanceItem, FriendAdapter.FriendViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTxt: TextView = itemView.findViewById(R.id.friendNameTxt)
        private val balanceTxt: TextView = itemView.findViewById(R.id.friendBalanceTxt)
        private val settleBtn: Button = itemView.findViewById(R.id.settleUpBtn)

        fun bind(item: FriendBalanceItem) {
            nameTxt.text = item.friendName
            balanceTxt.text = if (item.netBalance >= 0) "Owes me ₹${item.netBalance}" else "I owe ₹${-item.netBalance}"
            balanceTxt.setTextColor(if (item.netBalance >= 0) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())

            settleBtn.setOnClickListener {
                onSettleUp(item.friendId)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FriendBalanceItem>() {
        override fun areItemsTheSame(old: FriendBalanceItem, new: FriendBalanceItem) = old.friendId == new.friendId
        override fun areContentsTheSame(old: FriendBalanceItem, new: FriendBalanceItem) = old == new
    }
}

package com.arcjas.studentapp.ui.account

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.arcjas.studentapp.R
import com.arcjas.studentapp.core.model.Account

class AccountAdapter(
    private val context: Context,
    private var data: List<Account>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(account: Account)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_account_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = data[position]
        holder.bind(currentItem)
    }

    fun setData(newData: List<Account>) {
        data = newData
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textAccountName: TextView = itemView.findViewById(R.id.textAccountName)
        private val textAccountAmount: TextView = itemView.findViewById(R.id.textAccountAmount)
        private val btnDeleteAccount: Button = itemView.findViewById(R.id.btnDeleteAccount)

        fun bind(account: Account) {

            textAccountName.text = account.name
            textAccountAmount.text = "Monto Total: S/ ${account.balance}"
            btnDeleteAccount.setOnClickListener {
                listener.onItemClick(account)
            }

        }
    }
}

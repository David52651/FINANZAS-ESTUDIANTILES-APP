package com.arcjas.studentapp.ui.saving

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.arcjas.studentapp.core.model.Account

class AccountArrayAdapter(
    context: Context,
    @LayoutRes private val resource: Int,
    private val accounts: List<Account>
) : ArrayAdapter<Account>(context, resource, accounts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, resource)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup, @LayoutRes resource: Int): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val account = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = account?.name ?: "No Name"
        return view
    }
}

package com.arcjas.studentapp.ui.account

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcjas.studentapp.R
import com.arcjas.studentapp.core.config.VolleyListener
import com.arcjas.studentapp.core.model.Account
import com.arcjas.studentapp.core.model.ResponseCore
import com.arcjas.studentapp.core.model.Transaction
import com.arcjas.studentapp.core.service.AccountRequest
import com.arcjas.studentapp.core.service.ApiManager
import com.arcjas.studentapp.databinding.FragmentAccountBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AccountFragment : Fragment(), AccountAdapter.OnItemClickListener {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var accountRequest: AccountRequest
    private lateinit var adapter: AccountAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = AccountAdapter(requireContext(), emptyList(), this)
        recyclerView.adapter = adapter

        accountRequest =
            AccountRequest(requireContext(), ApiManager.getInstance(requireContext()))
        loadAccount()
        val btnPay: FloatingActionButton = binding.btnAddAccount;
        btnPay.setOnClickListener {
            showAddPaymentDialog()
        }

        return root
    }

    private fun loadAccount() {
        accountRequest.listAccount(object :
            VolleyListener<ResponseCore<List<Account>>> {
            override fun onSuccess(response: ResponseCore<ResponseCore<List<Account>>>) {
                println(response)
                val accounts = response.data?.data
                if (accounts != null) {
                    adapter.setData(accounts)
                }
            }

            override fun onError(error: String) {
                Toast.makeText(
                    requireContext(),
                    "Error retrieving transactions: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showAddPaymentDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_account)
        val txtAccountName: EditText = dialog.findViewById(R.id.txtAccountName)
        val btnSaveAccount: Button = dialog.findViewById(R.id.btnSaveAccount)
        btnSaveAccount.setOnClickListener {
            val txtAccountName = txtAccountName.text.toString()
            if (txtAccountName == "") {
                Toast.makeText(
                    requireContext(),
                    "Ingrese el nombre de la cuenta",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                accountRequest.createAccount(txtAccountName, object : VolleyListener<Account> {
                    override fun onSuccess(response: ResponseCore<Account>) {
                        Toast.makeText(
                            requireContext(),
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        if (response.success) {
                            loadAccount()
                            Toast.makeText(
                                requireContext(),
                                response.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                response.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }

                    override fun onError(error: String) {
                        Toast.makeText(
                            requireContext(),
                            "Error deleting account: $error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }

        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(account: Account) {
        accountRequest.deleteAccount(account, object : VolleyListener<String> {
            override fun onSuccess(response: ResponseCore<String>) {
                loadAccount()
                Toast.makeText(
                    requireContext(),
                    response.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(error: String) {
                Toast.makeText(
                    requireContext(),
                    "Error deleting account: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}

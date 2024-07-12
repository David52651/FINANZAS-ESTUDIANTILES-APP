package com.arcjas.studentapp.ui.saving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.arcjas.studentapp.R
import com.arcjas.studentapp.core.config.VolleyListener
import com.arcjas.studentapp.core.model.Account
import com.arcjas.studentapp.core.model.ResponseCore
import com.arcjas.studentapp.core.model.Saving
import com.arcjas.studentapp.core.service.AccountRequest
import com.arcjas.studentapp.core.service.ApiManager
import com.arcjas.studentapp.core.service.SavingRequest
import com.arcjas.studentapp.databinding.FragmentSavingBinding
import java.math.BigDecimal
import java.util.Locale

class SavingFragment : Fragment() {
    private var _binding: FragmentSavingBinding? = null
    private val binding get() = _binding!!
    private lateinit var selectedSourceAccount: String
    private lateinit var selectedDestinationAccount: String
    private var selectedFrequency: String = ""
    private var statusSaving: Boolean = false
    private lateinit var accountRequest: AccountRequest
    private lateinit var savingRequest: SavingRequest
    private lateinit var accountsList: List<Account>
    private lateinit var root: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavingBinding.inflate(inflater, container, false)
        root = binding.root
        initApi()
        setupButton()
        return root
    }

    private fun initApi() {
        accountRequest = AccountRequest(requireContext(), ApiManager.getInstance(requireContext()))
        savingRequest = SavingRequest(requireContext(), ApiManager.getInstance(requireContext()))
        loadAccount()
    }

    private fun loadAccount() {
        accountRequest.listAccount(object :
            VolleyListener<ResponseCore<List<Account>>> {
            override fun onSuccess(response: ResponseCore<ResponseCore<List<Account>>>) {
                val accounts = response.data?.data
                if (accounts != null) {
                    accountsList = accounts
                    setupSpinners()
                    loadSaving()

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

    private fun setupSpinners() {
        val accountAdapter = AccountArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            accountsList
        )
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSourceAccount.adapter = accountAdapter
        binding.spinnerSourceAccount.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSourceAccount = accountsList[position].id.toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedSourceAccount = accountsList[0].name ?: "No Name"
                }
            }
        binding.spinnerDestinationAccount.adapter = accountAdapter
        binding.spinnerDestinationAccount.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedDestinationAccount = accountsList[position].id.toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedDestinationAccount = accountsList[0].id.toString()
                }
            }

        val frequencies = listOf("5Segundos", "Diario", "Semanal", "Mensual")
        val frequencyAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencies)
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrequency.adapter = frequencyAdapter

        binding.spinnerFrequency.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedFrequency = frequencies[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedFrequency = frequencies[0]
                }
            }
    }

    private fun loadSaving() {
        savingRequest.loadSaving(object :
            VolleyListener<Saving> {
            override fun onSuccess(response: ResponseCore<Saving>) {
                val saving = response.data
                if (response.success && saving != null) {
                    val spinnerSourceAccount: Spinner = root.findViewById(R.id.spinnerSourceAccount)
                    val spinnerDestinationAccount: Spinner =
                        root.findViewById(R.id.spinnerDestinationAccount)
                    val editTextAmount: EditText = root.findViewById(R.id.editTextAmount)
                    val editTextGoal: EditText = root.findViewById(R.id.editTextGoal)
                    val spinnerFrequency: Spinner = root.findViewById(R.id.spinnerFrequency)
                    val editTextDescription: EditText = root.findViewById(R.id.editTextDescription)
                    val btnSavingStatus: Button = root.findViewById(R.id.btnSavingStatus)
                    statusSaving = saving.status
                    println(statusSaving)
                    btnSavingStatus.text = if (statusSaving) "Desactivar" else "Activar"
                    editTextAmount.setText(saving.amount.toString())
                    editTextGoal.setText(saving.savingGoal.toString())
                    editTextDescription.setText(saving.description)
                    if (::accountsList.isInitialized && accountsList.isNotEmpty()) {
                        val accountAdapter = AccountArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            accountsList
                        )
                        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                        spinnerSourceAccount.adapter = accountAdapter
                        spinnerDestinationAccount.adapter = accountAdapter
                        val sourceAccountIndex =
                            accountsList.indexOfFirst { it.id == saving.sourceAccountId }
                        val destinationAccountIndex =
                            accountsList.indexOfFirst { it.id == saving.destinationAccountId }

                        if (sourceAccountIndex != -1) {
                            spinnerSourceAccount.setSelection(sourceAccountIndex)
                        }
                        if (destinationAccountIndex != -1) {
                            spinnerDestinationAccount.setSelection(destinationAccountIndex)
                        }
                    }
                    spinnerFrequency.setSelection(getFrequencyIndex(saving.savingInterval))
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Hubo un error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onError(error: String) {
            }
        })
    }


    private fun setupButton() {
         var buttonStatus: Button = root.findViewById(R.id.btnSavingStatus)
        buttonStatus.setOnClickListener {
            savingRequest.updateStatusSaving(!statusSaving, object :
                VolleyListener<Saving> {
                override fun onSuccess(response: ResponseCore<Saving>) {
                    response.data
                    if (response.success) {
                        loadSaving()

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Hubo un error",
                            Toast.LENGTH_SHORT
                        ).show()
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

        binding.buttonStartSaving.setOnClickListener {

            val amount = binding.editTextAmount.text.toString().toDoubleOrNull()
            val goal = binding.editTextGoal.text.toString().toDoubleOrNull()
            val description = binding.editTextDescription.text.toString()

            if (amount != null && goal != null && description.isNotEmpty()) {
                accountsList.find { it.name == selectedSourceAccount }
                accountsList.find { it.id.toString() == selectedDestinationAccount }
                val saving = Saving(
                    id = null,
                    userId = 1L,
                    sourceAccountId = selectedSourceAccount.toLong(),
                    destinationAccountId = selectedDestinationAccount.toLong(),
                    amount = BigDecimal(amount),
                    savingGoal = BigDecimal(goal),
                    savingInterval = getSavingInterval(selectedFrequency),
                    description = description,
                    statusSaving,
                    lastProcessedAt = null,
                    createdAt = null,
                    updatedAt = null
                )
                println("SAVING")
                println(saving)
                if (saving.sourceAccountId != null && saving.destinationAccountId != null && amount != null) {
                    savingRequest.updateSaving(saving, object :
                        VolleyListener<Saving> {
                        override fun onSuccess(response: ResponseCore<Saving>) {
                            response.data
                            if (response.success) {
                                loadSaving()
                                Toast.makeText(
                                    requireContext(),
                                    "Se actualizo correctamente su ahorro",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Hubo un error",
                                    Toast.LENGTH_SHORT
                                ).show()
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

            } else {
                Toast.makeText(
                    requireContext(),
                    "Error: No se pudo encontrar las cuentas seleccionadas.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun getFrequencyIndex(savingInterval: Long): Int {
        return when (savingInterval) {
            5L -> 0 // Diario
            24 * 60 * 60L -> 1 // Diario
            7 * 24 * 60 * 60L -> 2 // Semanal
            30 * 24 * 60 * 60L -> 3 // Mensual
            else -> 0
        }
    }

    private fun getSavingInterval(frequency: String): Long {
        return when (frequency.lowercase(Locale.ROOT)) {
            "5segundos" -> 5 // 24 horas en segundos
            "diario" -> 24 * 60 * 60 // 24 horas en segundos
            "semanal" -> 7 * 24 * 60 * 60 // 7 días en segundos
            "mensual" -> 30 * 24 * 60 * 60 // 30 días en segundos (aproximado)
            else -> throw IllegalArgumentException("Frecuencia de ahorro no reconocida: $frequency")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

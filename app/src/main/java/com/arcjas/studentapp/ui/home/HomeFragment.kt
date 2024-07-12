package com.arcjas.studentapp.ui.home

import HomeAdapter
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcjas.studentapp.databinding.FragmentHomeBinding
import com.google.zxing.integration.android.IntentIntegrator
import android.util.Base64
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.arcjas.studentapp.R
import com.arcjas.studentapp.core.config.VolleyListener
import com.arcjas.studentapp.core.model.ResponseCore
import com.arcjas.studentapp.core.model.Transaction
import com.arcjas.studentapp.core.model.User
import com.arcjas.studentapp.core.service.ApiManager
import com.arcjas.studentapp.core.service.TransactionRequest
import org.json.JSONObject
import java.util.Calendar
import android.widget.ArrayAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment(), HomeAdapter.OnItemClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var selectedDate: Calendar? = null

    private lateinit var adapter: HomeAdapter
    private lateinit var transactionRequest: TransactionRequest
    private lateinit var apiManager: ApiManager
    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        apiManager = ApiManager.getInstance(requireContext())

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = HomeAdapter(requireContext(), emptyList(), this)
        recyclerView.adapter = adapter


        val btnPay: FloatingActionButton = binding.btnAddPayment;
        btnPay.setOnClickListener {
            showAddPaymentDialog()
        }
        val textAccountAmount: TextView = root.findViewById(R.id.textViewUserName)
        val user = apiManager.getCurrentUser()
        user?.let {
            val spannableText =
                SpannableString("Hola ${it.name} \uD83D\uDC4B, Bienvenido a StudentAPP \uD83E\uDDD1\u200D\uD83C\uDF93")
            val startIndex = it.name?.let { it1 -> spannableText.indexOf(it1) }
            if (startIndex != null) {
                spannableText.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    startIndex + it.name.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            textAccountAmount.text = spannableText
        }
        transactionRequest =
            TransactionRequest(requireContext(), ApiManager.getInstance(requireContext()))
        loadTransactions()
        //user = apiManager.getCurrentUser()!!
        val spinner: Spinner = root.findViewById(R.id.spinnerTransactionType)
        val transactionTypes = arrayOf("Todos", "Ingreso", "Egreso")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, transactionTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        var valueType = "Todos";
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                valueType = transactionTypes[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val btnSearch: Button = binding.btnSearch
        btnSearch.setOnClickListener {
            println(valueType)
            println(selectedDate?.time)
            loadTransactionsByFilter(valueType, selectedDate?.time.toString())
        }
        val btnScan: Button = binding.btnScanQR
        btnScan.setOnClickListener {
            iniciarEscaneoQR()
        }

        val btnSelectDate: Button = binding.btnSelectDate
        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                btnSelectDate.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            }, year, month, day).show()
        }
        return root
    }

    private fun loadTransactions() {
        transactionRequest.listTransactions(object :
            VolleyListener<ResponseCore<List<Transaction>>> {
            override fun onSuccess(response: ResponseCore<ResponseCore<List<Transaction>>>) {
                println(response)
                val transactions = response.data?.data
                if (transactions != null) {
                    adapter.setData(transactions)
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
        dialog.setContentView(R.layout.dialog_add_payment)

        val spinnerPaymentType: Spinner = dialog.findViewById(R.id.spinnerPaymentType)
        val editTextAmount: EditText = dialog.findViewById(R.id.editTextAmount)
        val buttonPay: Button = dialog.findViewById(R.id.buttonPay)
        val paymentTypes = listOf(
            Pair("Pago de Universidad", "1"),
            Pair("Pago de Agua", "2"),
            Pair("Pago de Luz", "3"),
            Pair("Pago de Internet", "4")
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            paymentTypes.map { it.first }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPaymentType.adapter = adapter

        buttonPay.setOnClickListener {
            val selectedPosition = spinnerPaymentType.selectedItemPosition
            val selectedPaymentType = paymentTypes[selectedPosition]
            val paymentName = selectedPaymentType.first
            val amount = editTextAmount.text.toString()
            if (amount.isNotEmpty()) {
                transactionRequest.createTransaction(paymentName, amount, object :
                    VolleyListener<Transaction> {
                    override fun onSuccess(response: ResponseCore<Transaction>) {
                        loadTransactions()

                        Toast.makeText(
                            requireContext(),
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onError(error: String) {
                        Toast.makeText(
                            requireContext(),
                            error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
                dialog.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Por favor, completa todos los campos",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
        dialog.show()
    }

    private fun loadTransactionsByFilter(type: String, date: String) {
        transactionRequest.listTransactionsByFilter(type, date, object :
            VolleyListener<ResponseCore<List<Transaction>>> {
            override fun onSuccess(response: ResponseCore<ResponseCore<List<Transaction>>>) {
                println(response)
                val transactions = response.data?.data
                if (transactions != null) {
                    adapter.setData(transactions)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(item: Transaction) {
        Toast.makeText(
            requireContext(),
            "Elemento seleccionado: ${item.amount}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun iniciarEscaneoQR() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Escanea un código QR")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(false)
        integrator.setOrientationLocked(false)
        integrator.setBarcodeImageEnabled(true)
        integrator.initiateScan()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            } else {
                val qrContent = result.contents
                mostrarDatosDesencriptados(qrContent)
            }
        }
    }

    private fun mostrarDatosDesencriptados(qrContent: String) {
        try {
            val decodedBytes = Base64.decode(qrContent, Base64.DEFAULT)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            val jsonObject = JSONObject(decodedString)
            val id = jsonObject.getInt("id")
            val name = jsonObject.getString("name")
            val description = jsonObject.getString("description")
            val transactionJsonObject = JSONObject().apply {
                put("product_id", id)
            }
            transactionRequest.createTransaction(
                transactionJsonObject,
                object : VolleyListener<ResponseCore<String>> {
                    override fun onSuccess(response: ResponseCore<ResponseCore<String>>) {
                        Toast.makeText(
                            requireContext(),
                            "Transacción creada exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadTransactions()
                    }


                    override fun onError(error: String) {
                        Toast.makeText(
                            requireContext(),
                            "Error al crear la transacción: $error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al decodificar el QR", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
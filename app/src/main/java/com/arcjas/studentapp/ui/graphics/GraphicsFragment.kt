package com.arcjas.studentapp.ui.graphics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.arcjas.studentapp.core.config.VolleyListener
import com.arcjas.studentapp.core.model.Account
import com.arcjas.studentapp.core.model.ResponseCore
import com.arcjas.studentapp.core.model.Transaction
import com.arcjas.studentapp.core.service.AccountRequest
import com.arcjas.studentapp.core.service.ApiManager
import com.arcjas.studentapp.core.service.TransactionRequest
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.arcjas.studentapp.databinding.FragmentGraphicsBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class GraphicsFragment : Fragment() {
    private var _binding: FragmentGraphicsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionRequest: TransactionRequest
    private lateinit var accountRequest: AccountRequest

    private val entriesPie = ArrayList<PieEntry>()
    private val entriesIncome = mutableListOf<BarEntry>()
    private val entriesExpense = mutableListOf<BarEntry>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphicsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        transactionRequest =
            TransactionRequest(requireContext(), ApiManager.getInstance(requireContext()))
        accountRequest =
            AccountRequest(requireContext(), ApiManager.getInstance(requireContext()))

        loadTransactions()
        loadAccounts()

        return root
    }

    private fun setupPieChart(entries: List<PieEntry>) {
        val pieChart: PieChart = binding.pieChart

        val dataSetPie = PieDataSet(entries, "Balance")
        dataSetPie.setDrawValues(true)
        dataSetPie.valueTextSize = 12f
        dataSetPie.valueTextColor = Color.BLACK

        // Usar colores automáticos de la biblioteca MPAndroidChart
        dataSetPie.colors = ColorTemplate.COLORFUL_COLORS.toList()

        val formatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.0f", value)
            }
        }

        dataSetPie.valueFormatter = formatter
        val pieData = PieData(dataSetPie)
        pieChart.data = pieData

        pieChart.setUsePercentValues(false)
        pieChart.description.isEnabled = false
        pieChart.centerText = "Balance"
        pieChart.setCenterTextSize(18f)
        pieChart.setCenterTextColor(Color.BLACK)
        pieChart.setCenterTextOffset(0f, -20f)
        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)

        pieChart.invalidate()
    }




    private fun loadTransactions() {
        transactionRequest.listTransactions(object :
            VolleyListener<ResponseCore<List<Transaction>>> {
            override fun onSuccess(response: ResponseCore<ResponseCore<List<Transaction>>>) {
                println(response)
                val transactions = response.data?.data
                if (transactions != null) {
                    setupBarChart(transactions)
                    setupLineCharts(transactions)
                }
            }

            override fun onError(error: String) {
                Toast.makeText(
                    requireContext(),
                    "Error al obtener transacciones: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupLineCharts(transactions: List<Transaction>) {
        val entriesIncome = mutableListOf<Entry>()
        val entriesExpense = mutableListOf<Entry>()

        // Ordenar transacciones por ID ascendente
        val sortedTransactions = transactions.sortedBy { it.id }

        var currentIncome = 0f
        var currentExpense = 0f

        sortedTransactions.forEachIndexed { index, transaction ->
            transaction.amount?.let { amount ->
                if (transaction.type == "income") {
                    currentIncome += amount.toFloat()
                } else if (transaction.type == "expense") {
                    currentExpense += amount.toFloat()
                }
            }
            entriesIncome.add(Entry(transaction.id.toFloat(), currentIncome))
            entriesExpense.add(Entry(transaction.id.toFloat(), -currentExpense))
        }

        val lineChart1: LineChart = binding.lineChart1
        val lineChart2: LineChart = binding.lineChart2

        // Configurar gráfico de líneas para ingresos (lineChart1)
        setupLineChart(lineChart1, entriesIncome, "Ingresos", Color.GREEN)

        // Configurar gráfico de líneas para gastos (lineChart2)
        setupLineChart(lineChart2, entriesExpense, "Gastos", Color.RED)
    }

    private fun setupLineChart(
        lineChart: LineChart,
        entries: List<Entry>,
        label: String,
        color: Int
    ) {
        val dataSet = LineDataSet(entries, label)
        dataSet.color = color
        dataSet.setCircleColor(color)
        dataSet.setDrawCircleHole(false)

        val lineData = LineData(dataSet)

        lineChart.data = lineData

        val description = Description()
        description.text = label
        lineChart.description = description

        // Configurar el eje X para mostrar el ID de la transacción
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter()

        lineChart.invalidate()
    }


    private fun setupBarChart(
        transactions: List<Transaction>
    ) {
        var totalIncome = 0f
        var totalExpense = 0f
        transactions.forEach { transaction ->
            transaction.amount?.let {
                if (transaction.type == "income") {
                    totalIncome += it.toFloat()
                } else if (transaction.type == "expense") {
                    totalExpense += it.toFloat()
                }
            }
        }
        val barEntryIncome = BarEntry(1f, totalIncome)
        val barEntryExpense = BarEntry(2f, totalExpense)
        entriesIncome.clear()
        entriesExpense.clear()
        entriesIncome.add(barEntryIncome)
        entriesExpense.add(barEntryExpense)
        val barChart: BarChart = binding.barChart1

        val barDataSetIncome = BarDataSet(entriesIncome, "Ingresos")
        barDataSetIncome.color = Color.GREEN

        val barDataSetExpense = BarDataSet(entriesExpense, "Egresos")
        barDataSetExpense.color = Color.RED

        val barData = BarData(barDataSetIncome, barDataSetExpense)
        barChart.data = barData

        val description = Description()
        description.text = "Ingresos y Egresos"
        barChart.description = description

        barChart.setFitBars(true)
        barChart.invalidate()
    }

    private fun loadAccounts() {
        accountRequest.listAccount(object :
            VolleyListener<ResponseCore<List<Account>>> {
            override fun onSuccess(response: ResponseCore<ResponseCore<List<Account>>>) {
                println(response)
                val accounts = response.data?.data
                if (accounts != null) {
                    // Limpiar la lista antes de agregar nuevas entradas
                    entriesPie.clear()

                    // Lista de colores predefinidos
                    val colors = mutableListOf<Int>(
                        Color.rgb(255, 102, 0),    // Naranja
                        Color.rgb(255, 204, 0),    // Amarillo
                        Color.rgb(51, 204, 204),   // Verde agua
                        Color.rgb(153, 102, 255),  // Morado
                        Color.rgb(255, 51, 153)    // Rosa
                        // Añadir más colores según sea necesario
                    )

                    // Ordenar las cuentas por fecha de creación si es necesario
                    accounts.sortedBy { it.createdAt?.let { parseDate(it) } }
                        .forEachIndexed { index, account ->
                            account.balance?.let { balance ->
                                val color = colors[index % colors.size]
                                entriesPie.add(PieEntry(balance.toFloat(), account.name ?: "", color))
                            }
                        }
                    setupPieChart(entriesPie)
                }
            }

            override fun onError(error: String) {
                Toast.makeText(
                    requireContext(),
                    "Error al obtener cuentas: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun parseDate(dateString: String): Date {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        return formatter.parse(dateString) ?: Date()
    }
}

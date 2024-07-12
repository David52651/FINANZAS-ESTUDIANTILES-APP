import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arcjas.studentapp.R
import com.arcjas.studentapp.core.model.Transaction

class HomeAdapter(
    private val context: Context,
    private var data: List<Transaction>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(transaction: Transaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.financial_movements_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = data[position]
        holder.bind(currentItem)

        holder.itemView.setOnClickListener {
            listener.onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(newData: List<Transaction>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtId: TextView = itemView.findViewById(R.id.txtId)
        private val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        private val textView2: TextView = itemView.findViewById(R.id.textView2)
        private val montoTextView: TextView = itemView.findViewById(R.id.montoTextView)

        fun bind(transaction: Transaction) {
            txtId.text = transaction.id.toString()
            nombreTextView.text = transaction.description
            textView2.text = "\uD83D\uDCC5 : "+transaction.createdAtFormatted
            montoTextView.text = transaction.amount

            if (transaction.type == "income") {
                montoTextView.setTextColor(context.resources.getColor(R.color.green))
                montoTextView.text = "+ S/ ${transaction.amount}"
            } else {
                montoTextView.setTextColor(context.resources.getColor(R.color.red))
                montoTextView.text = "- S/ ${transaction.amount}"
            }
        }
    }
}

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dss.ipcontrol.R

class DataAdapter(private val data: ArrayList<String>) :
    RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return DataViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.textTv.text = data[position]
    }

    override fun getItemCount(): Int = data.size

    fun updateDataList(list: List<String>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTv: TextView = itemView.findViewById(R.id.textTv)
    }
}
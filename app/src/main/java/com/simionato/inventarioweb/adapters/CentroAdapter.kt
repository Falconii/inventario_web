package com.simionato.inventarioweb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.models.CentroCustoModel

class CentroAdapter(private val lista : List<CentroCustoModel>) :
    RecyclerView.Adapter<CentroAdapter.PesquisaViewHolder>(),Filterable {

    private var listaFiltered = lista
    inner class PesquisaViewHolder(val ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val  textDescricao : TextView
        val  txtSubTitulo: TextView

        init {
            textDescricao = ItemView.findViewById(R.id.txtDescricaoItem)
            txtSubTitulo  = ItemView.findViewById(R.id.txtSubTitulo)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesquisaViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        val itemView = layoutInflater.inflate(R.layout.itemlista,parent,false)

        return PesquisaViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listaFiltered.size
    }

    override fun onBindViewHolder(listaViewHolder: PesquisaViewHolder, position: Int) {

        listaViewHolder.textDescricao.setText(listaFiltered[position].codigo)
        listaViewHolder.txtSubTitulo.setText(listaFiltered[position].descricao)
    }

    override fun getFilter(): Filter {
        return pesquisaFilter
    }

    private val pesquisaFilter = object : Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val text = constraint.toString().orEmpty()

            val resultList = ArrayList<CentroCustoModel>()

            if (text.isEmpty()){
                resultList.addAll(lista)
            } else {
                lista
                    .filter { it.descricao.lowercase().contains(text.lowercase())}
                    .forEach({obj -> resultList.add(obj) })
            }
            return FilterResults().apply {
                values = resultList
                count= resultList.size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

            val result = if (results?.values == null){
                ArrayList()
            } else {
                results.values as ArrayList<CentroCustoModel>
            }
            setNewData(result)
        }

    }

    fun setNewData(data:List<CentroCustoModel>){
        listaFiltered = data.orEmpty()
        notifyDataSetChanged()
    }
}

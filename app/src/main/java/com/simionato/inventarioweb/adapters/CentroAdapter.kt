package com.simionato.inventarioweb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.models.CentroCustoModel

class CentroAdapter(
    private val lista : List<CentroCustoModel>,
    private val clique: (centro:CentroCustoModel) -> Unit
    ) :
    RecyclerView.Adapter<CentroAdapter.PesquisaViewHolder>(),Filterable {

    private var listaFiltered = lista
    inner class PesquisaViewHolder(val ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val  layout: View
        val  textDescricao : TextView
        val  txtSubTitulo: TextView

        init {
            layout =  ItemView.findViewById(R.id.llMainItemLista100)
            textDescricao = ItemView.findViewById(R.id.txtDescricaoItemLista100)
            txtSubTitulo  = ItemView.findViewById(R.id.txtSubTituloItemLista100)
        }

        fun bind(centro:CentroCustoModel){
            textDescricao.setText(ParametroGlobal.prettyText.tituloDescricao("Código: ",centro.codigo.toString()))
            txtSubTitulo.setText(ParametroGlobal.prettyText.tituloDescricao("Descrição: ",centro.descricao,true))

            layout.setOnClickListener {
                clique(centro)
            }
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

        val centro = listaFiltered[position]

        listaViewHolder.bind(centro)

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

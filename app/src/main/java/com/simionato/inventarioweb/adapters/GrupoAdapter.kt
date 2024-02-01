package com.simionato.inventarioweb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.models.GrupoModel

class GrupoAdapter(
    private val lista : List<GrupoModel>,
    private val clique: (grupo:GrupoModel) -> Unit
    ) :
    RecyclerView.Adapter<GrupoAdapter.PesquisaViewHolder>(), Filterable {

    private var listaFiltered = lista
    inner class PesquisaViewHolder(val ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val  layout:View
        val  txtDescricao : TextView
        val  txtSubTitulo: TextView

        init {
            layout       = ItemView.findViewById(R.id.llMainItemLista100)
            txtDescricao = ItemView.findViewById(R.id.txtDescricaoItemLista100)
            txtSubTitulo  = ItemView.findViewById(R.id.txtSubTituloItemLista100)
        }

        fun bind(grupo:GrupoModel){
            txtDescricao.setText(grupo.codigo.toString())
            txtSubTitulo.setText(grupo.descricao)
            layout.setOnClickListener{
                clique(grupo)
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

        val grupo = listaFiltered[position]

        listaViewHolder.bind(grupo)
    }

    override fun getFilter(): Filter {
        return pesquisaFilter
    }

    private val pesquisaFilter = object : Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val text = constraint.toString().orEmpty()

            val resultList = ArrayList<GrupoModel>()

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
                results.values as ArrayList<GrupoModel>
            }
            setNewData(result)
        }

    }

    fun setNewData(data:List<GrupoModel>){
        listaFiltered = data.orEmpty()
        notifyDataSetChanged()
    }
}
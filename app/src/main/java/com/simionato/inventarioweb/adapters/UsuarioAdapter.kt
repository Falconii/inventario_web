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
import com.simionato.inventarioweb.models.UsuarioQuery01Model

class UsuarioAdapter(
    private val lista : List<UsuarioQuery01Model>,
    private val clique: (usuario:UsuarioQuery01Model) -> Unit) :
    RecyclerView.Adapter<UsuarioAdapter.PesquisaViewHolder>(),Filterable {

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

        fun bind(usuario:UsuarioQuery01Model ){
            textDescricao.setText(ParametroGlobal.prettyText.tituloDescricao("Código: ",usuario.id.toString().padStart(6,'0')))
            txtSubTitulo.setText(ParametroGlobal.prettyText.tituloDescricao("Descrição: ",usuario.razao,true))
            layout.setOnClickListener {
                clique(usuario)
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

        val usuario = listaFiltered[position]

        listaViewHolder.bind(usuario)
    }

    override fun getFilter(): Filter {
        return pesquisaFilter
    }

    private val pesquisaFilter = object :Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
           val text = constraint.toString().orEmpty()

            val resultList = ArrayList<UsuarioQuery01Model>()

            if (text.isEmpty()){
                resultList.addAll(lista)
            } else {
                lista
                    .filter { it.razao.lowercase().contains(text.lowercase())}
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
                results.values as ArrayList<UsuarioQuery01Model>
            }
            setNewData(result)
        }

    }

    fun setNewData(data:List<UsuarioQuery01Model>){
        listaFiltered = data.orEmpty()
        notifyDataSetChanged()
    }
}
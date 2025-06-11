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
import com.simionato.inventarioweb.models.FotoUploadModel

class FotoUploadAdapter(
    private val lista : List<FotoUploadModel>,
    private val clique: (foto:FotoUploadModel) -> Unit) :
    RecyclerView.Adapter<FotoUploadAdapter.PesquisaViewHolder>(),Filterable {

    private var listaFiltered = lista
    inner class PesquisaViewHolder(val ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val  layout: View
        val  foto_item_load_txt_obs: TextView
        var  foto_item_load_txt_usuario:TextView

        init {
            layout =  ItemView.findViewById(R.id.ll_load_foto)
            foto_item_load_txt_obs  = ItemView.findViewById(R.id.foto_item_load_txt_obs)
            foto_item_load_txt_usuario = ItemView.findViewById(R.id.foto_item_load_txt_usuario)

        }

        fun bind(foto: FotoUploadModel){
            foto_item_load_txt_obs.setText(ParametroGlobal.prettyText.tituloDescricao("Obs: ",foto.obs,true))
            foto_item_load_txt_usuario.setText(ParametroGlobal.prettyText.tituloDescricao("Usuário: ",foto.idUsuario.toString(),true))

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesquisaViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        val itemView = layoutInflater.inflate(R.layout.item_load_foto,parent,false)

        return PesquisaViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listaFiltered.size
    }

    override fun onBindViewHolder(listaViewHolder: PesquisaViewHolder, position: Int) {

        val foto = listaFiltered[position]

        listaViewHolder.bind(foto)
    }

    override fun getFilter(): Filter {
        return pesquisaFilter
    }

    private val pesquisaFilter = object :Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val text = constraint.toString().orEmpty()

            val resultList = ArrayList<FotoUploadModel>()

            if (text.isEmpty()){
                resultList.addAll(lista)
            } else {
                lista
                    .filter { it.fileNameOriginal .lowercase().contains(text.lowercase())}
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
                results.values as ArrayList<FotoUploadModel>
            }
            setNewData(result)
        }

    }

    fun setNewData(data:List<FotoUploadModel>){
        listaFiltered = data.orEmpty()
        notifyDataSetChanged()
    }
}
package com.simionato.inventarioweb.adapters

import android.telephony.CarrierConfigManager.ImsEmergency
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.models.CentroCustoMultiModel

class CentroMultiAdapter(
    private val lista : List<CentroCustoMultiModel>,
    private val clique: (centro:CentroCustoMultiModel) -> Unit,
    private val check :  (centro:CentroCustoMultiModel, check:Boolean) -> Unit
    ) :
    RecyclerView.Adapter<CentroMultiAdapter.PesquisaViewHolder>(),Filterable {

    private var listaFiltered = lista
    inner class PesquisaViewHolder(val ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val  layout: View
        val  textDescricao : TextView
        val  txtSubTitulo: TextView
        val   checkBox: CheckBox

        init {
            layout =  ItemView.findViewById(R.id.llMainItemLista101)
            checkBox = ItemView.findViewById(R.id.cbItem101)
            textDescricao = ItemView.findViewById(R.id.txtDescricaoItemLista101)
            txtSubTitulo  = ItemView.findViewById(R.id.txtSubTituloItemLista101)
        }

        fun bind(centro:CentroCustoMultiModel){
            checkBox.isChecked = centro.check
            textDescricao.setText(ParametroGlobal.prettyText.tituloDescricao("Código: ",centro.centro_custo.codigo.toString()))
            txtSubTitulo.setText(ParametroGlobal.prettyText.tituloDescricao("Descrição: ",centro.centro_custo.descricao,true))

            layout.setOnClickListener {
                clique(centro)
            }

            checkBox.setOnClickListener{
                centro.check = checkBox.isChecked
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesquisaViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        val itemView = layoutInflater.inflate(R.layout.itemccmulti,parent,false)

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

            val resultList = ArrayList<CentroCustoMultiModel>()

            if (text.isEmpty()){
                resultList.addAll(lista)
            } else {
                lista
                    .filter { ParametroGlobal.Acentos.semAcento(it.centro_custo.descricao.lowercase()).contains(
                        ParametroGlobal.Acentos.semAcento(text.lowercase()))}
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
                results.values as ArrayList<CentroCustoMultiModel>
            }
            setNewData(result)
        }

    }

    fun setNewData(data:List<CentroCustoMultiModel>){
        listaFiltered = data.orEmpty()
        notifyDataSetChanged()
    }
}

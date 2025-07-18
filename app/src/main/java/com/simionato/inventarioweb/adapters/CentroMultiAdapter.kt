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
    ) :
    RecyclerView.Adapter<CentroMultiAdapter.PesquisaViewHolder>(),Filterable {

    private var listaFiltered = lista


    val filterCheck: Filter
        get() = pesquisaFilterCheck

    inner class PesquisaViewHolder(val ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val  layout: View
        val  textDescricao : TextView
        val  txtSubTitulo: TextView
        val  checkBox: CheckBox

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
                //clique(centro)
            }

            checkBox.setOnClickListener{
                centro.check = checkBox.isChecked
                atualizaListaOriginal(centro)
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

    private val pesquisaFilterCheck = object : Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val check = constraint.toString().orEmpty()

            val resultList = ArrayList<CentroCustoMultiModel>()

            if (check.isEmpty()){
                resultList.addAll(lista)
            } else {
                val validar = if (check == "T") {true} else {false}
                lista
                    .filter { (it.check == validar)}
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

    fun setMarcacao(flag:Boolean) {
        listaFiltered.forEach { cc -> cc.check = flag }
        sincronizarCheck()
        notifyDataSetChanged()
    }
    fun setNewData(data:List<CentroCustoMultiModel>){
        listaFiltered = data.orEmpty()
        notifyDataSetChanged()
    }

    private fun atualizaListaOriginal(centro:CentroCustoMultiModel){
        val indice = lista.indexOfFirst { cc -> cc.centro_custo.codigo == centro.centro_custo.codigo }
        if (indice != -1){
            lista.get(indice).check = centro.check
        }

    }

    fun sincronizarCheck() {
        for (itemFiltrado in listaFiltered) {
            val itemOriginal = lista.find { it.centro_custo.codigo == itemFiltrado.centro_custo.codigo }
            if (itemOriginal != null) {
                itemOriginal.check = itemFiltrado.check
            }
        }
    }

    fun EstaTudoMarcado():Boolean{
        var retorno = false

        val listaMarcada = lista.filter{ cc -> cc.check }

        if (listaMarcada.size == lista.size) {
            return true
        }

        return retorno
    }
}

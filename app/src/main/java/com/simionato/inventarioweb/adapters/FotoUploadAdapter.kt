package com.simionato.inventarioweb.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.models.FotoUploadModel

class FotoUploadAdapter(
    private val lista : List<FotoUploadModel>,
    private val clique: (foto:FotoUploadModel,idAcao:CadastrosAcoes) -> Unit) :
    RecyclerView.Adapter<FotoUploadAdapter.PesquisaViewHolder>(),Filterable {

    private var listaFiltered = lista
    inner class PesquisaViewHolder(val ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val  layout: View
        val  foto_item_load_image: ImageView
        val btUpload:ImageButton
        val  btDestaque:ImageButton
        val  btShow:ImageButton
        val  btDelete:ImageButton
        val  btUpdate:ImageButton
        val  foto_item_load_txt_ativo : TextView
        val  foto_item_load_txt_descricao : TextView
        val  foto_item_load_txt_obs: TextView
        val  foto_item_load_txt_usuario:TextView
        val foto_item_load_txt_situacao:TextView

        init {
            layout =  ItemView.findViewById(R.id.ll_load_foto)
            btUpload   = ItemView.findViewById(R.id.foto_item_load_upload)
            btDestaque = ItemView.findViewById(R.id.foto_item_load_destaque)
            btDelete     = ItemView.findViewById(R.id.foto_item_load_delete)
            btUpdate     = ItemView.findViewById(R.id.foto_item_load_edicao)
            btShow     = ItemView.findViewById(R.id.foto_item_load_consulta)
            foto_item_load_txt_ativo = ItemView.findViewById(R.id.foto_item_load_txt_ativo)
            foto_item_load_image = ItemView.findViewById(R.id.foto_item_load_image)
            foto_item_load_txt_descricao = ItemView.findViewById(R.id.foto_item_load_txt_descricao)
            foto_item_load_txt_obs  = ItemView.findViewById(R.id.foto_item_load_txt_obs)
            foto_item_load_txt_usuario = ItemView.findViewById(R.id.foto_item_load_txt_usuario)
            foto_item_load_txt_situacao = ItemView.findViewById(R.id.foto_item_load_txt_situacao)
        }

        fun bind(foto: FotoUploadModel){

            val fotoUri = Uri.parse(foto.idFile)

            Glide.with(layout.context)
                .load(fotoUri)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.imagem_falha)
                .into(foto_item_load_image)

            btDestaque.visibility = if (foto.destaque == "S")  View.VISIBLE else View.GONE
            foto_item_load_txt_ativo.setText(ParametroGlobal.prettyText.tituloDescricao("Código: ",foto.idImobilizado.toString().padStart(6,'0'),false))
            foto_item_load_txt_descricao.setText(ParametroGlobal.prettyText.tituloDescricao("Descrição: ",foto.descricao,true))
            foto_item_load_txt_obs.setText(ParametroGlobal.prettyText.tituloDescricao("Obs: ",foto.obs,true))
            foto_item_load_txt_usuario.setText(ParametroGlobal.prettyText.tituloDescricao("Usuário: ",foto.razao.toString(),true))
            foto_item_load_txt_situacao.setText(ParametroGlobal.prettyText.tituloDescricao("Situação: ","Foto Aguardando UPLOAD",true))

            btUpload.setOnClickListener{
                clique(foto,CadastrosAcoes.UpLoadFoto)
            }
            btShow.setOnClickListener {
                clique(foto, CadastrosAcoes.Consulta)
            }

            btDelete.setOnClickListener {
                clique(foto, CadastrosAcoes.Exclusao)
            }

            btUpdate.setOnClickListener {
                clique(foto, CadastrosAcoes.Edicao)
            }
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
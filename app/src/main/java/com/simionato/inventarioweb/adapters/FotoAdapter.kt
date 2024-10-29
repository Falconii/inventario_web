package com.simionato.inventarioweb.adapters
/*
  https://stackoverflow.com/questions/37462869/strange-issue-with-loading-image-from-imageview
 */

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.models.FotoModel
import java.net.URL


class FotoAdapter(
    private val clique: (foto: FotoModel,idAcao:CadastrosAcoes) -> Unit
): RecyclerView.Adapter<FotoAdapter.FotoViewHolder>(){
    var lista : List<FotoModel> = listOf()
    inner class FotoViewHolder(val ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val  layout: View
        val  btDestaque:ImageButton
        val  btShow:ImageButton
        val  btDelete:ImageButton
        val  btUpdate:ImageButton
        val  image:ImageView
        val  textDescricao : TextView
        val  textObservacao : TextView
        val  textUsuario: TextView
        init {
            layout =  ItemView.findViewById(R.id.foto_item_layout)
            image =  ItemView.findViewById(R.id.foto_item_image)
            btDestaque = ItemView.findViewById(R.id.foto_item_destaque)
            btDelete     = ItemView.findViewById(R.id.foto_item_delete)
            btUpdate     = ItemView.findViewById(R.id.foto_item_edicao)
            btShow     = ItemView.findViewById(R.id.foto_item_consulta)
            textDescricao = ItemView.findViewById(R.id.foto_item_txt_descricao)
            textObservacao = ItemView.findViewById(R.id.foto_item_txt_obs)
            textUsuario  = ItemView.findViewById(R.id.foto_item_txt_usuario)

            btUpdate.visibility = View.GONE;
        }
        fun bind(foto:FotoModel){
            try {
                val url = URL("https://drive.google.com/uc?export=view&id=${foto.id_file}")
                val thumbnailUrl = URL("https://drive.google.com/thumbnail?id=${foto.id_file}&sz=w500")


                Glide.with(layout.context)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.no_foto)
                    .into(image);
                /*
                Glide.with(layout.context)
               .load(url)
               .centerCrop()

                    .placeholder(R.drawable.image_loading)
                    .error(R.drawable.no_foto)
               .into(image)
               */

            } catch (e:Exception){
                Log.i("zyzz","Erro-> ${e.message}")
            }
            textDescricao.setText(foto.imo_descricao)
            textObservacao.setText(foto.obs)
            textUsuario.setText(foto.usu_razao)

            btDestaque.visibility = if (foto.destaque == "S")  View.VISIBLE else View.GONE

            image.setOnClickListener{
                clique(foto,CadastrosAcoes.Consulta)
            }

            btShow.setOnClickListener {
               clique(foto,CadastrosAcoes.Consulta)
            }


            btDelete.setOnClickListener {
                clique(foto,CadastrosAcoes.Exclusao)
            }

            btUpdate.setOnClickListener {
               clique(foto,CadastrosAcoes.Edicao)
           }

        }
    }


override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {

    val layoutInflater = LayoutInflater.from(parent.context)

    val itemView = layoutInflater.inflate(R.layout.fotoitem,parent,false)

    return FotoViewHolder(itemView)
}

override fun getItemCount(): Int {
    return lista.size
}

override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {

    val foto = lista[position]

    holder.bind(foto)
}


}
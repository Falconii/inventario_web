package com.simionato.inventarioweb.adapters
/*
  https://stackoverflow.com/questions/37462869/strange-issue-with-loading-image-from-imageview
 */

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.models.FotoModel
import java.net.URL


class FotoAdapter(
    private val lista : List<FotoModel>,
    private val clique: (foto: FotoModel) -> Unit
): RecyclerView.Adapter<FotoAdapter.FotoViewHolder>(){

    inner class FotoViewHolder(val ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val  layout: View
        val  image:ImageView
        val  textDescricao : TextView
        val  textObservacao : TextView
        val  textUsuario: TextView

        init {
            layout =  ItemView.findViewById(R.id.foto_item_layout)
            image =  ItemView.findViewById(R.id.foto_item_image)
            textDescricao = ItemView.findViewById(R.id.foto_item_txt_descricao)
            textObservacao = ItemView.findViewById(R.id.foto_item_txt_obs)
            textUsuario  = ItemView.findViewById(R.id.foto_item_txt_usuario)
        }

        fun bind(foto:FotoModel){
            try {
                val url = URL("https://drive.google.com/uc?export=view&id=${foto.id_file}")
                val thumbnailUrl = URL("https://drive.google.com/thumbnail?id=${foto.id_file}&sz=w10O")

                Glide.with(layout.context)
                    .load(url)
                    .thumbnail(
                        Glide.with(layout.context)
                            .load(thumbnailUrl))
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
            textDescricao.setText("Ainda Não Tenho")
            textObservacao.setText(foto.obs)
            textUsuario.setText("Ainda Não Tenho")

            layout.setOnClickListener {
               clique(foto)
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
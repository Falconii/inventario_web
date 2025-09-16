package com.simionato.inventarioweb.adapters
/*
  https://stackoverflow.com/questions/37462869/strange-issue-with-loading-image-from-imageview
 */

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ortiz.touchview.TouchImageView
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.models.FotoModel
import java.net.URL


class FotosAtivosAdapter(): RecyclerView.Adapter<FotosAtivosAdapter.FotoViewHolder>(){
    var lista : List<FotoModel> = listOf()
    inner class FotoViewHolder(val ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val  layout: View
        val  image: TouchImageView
        init {
            layout =  ItemView.findViewById(R.id.foto_ativo_item_layout)
            image =  ItemView.findViewById(R.id.foto_ativo_item_image)
        }
        fun bind(foto:FotoModel){
            try {
                if (foto.localizacao == "D"){
                    val fotoUri = Uri.parse(foto.id_file)
                    Glide.with(layout.context)
                        .load(fotoUri)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.imagem_falha)
                        .into(image)
                } else {
                    val url = URL("https://drive.google.com/uc?export=view&id=${foto.id_file}")
                    val thumbnailUrl = URL("https://drive.google.com/thumbnail?id=${foto.id_file}&sz=w500")
                    Glide.with(layout.context)
                        .load(thumbnailUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .into(image);
                }

            } catch (e:Exception){
                Log.i("zyzz","Erro-> ${e.message}")
            }

        }
    }


override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {

    val layoutInflater = LayoutInflater.from(parent.context)

    val itemView = layoutInflater.inflate(R.layout.item_foto_ativo,parent,false)

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
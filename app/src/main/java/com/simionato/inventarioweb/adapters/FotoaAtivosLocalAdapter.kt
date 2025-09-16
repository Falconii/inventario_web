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
import com.simionato.inventarioweb.models.FotoUploadModel
import java.net.URL


class FotoaAtivosLocalAdapter(): RecyclerView.Adapter<FotoaAtivosLocalAdapter.FotoViewHolder>(){
    var lista : List<FotoUploadModel> = listOf()
    inner class FotoViewHolder(val ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val  layout: View
        val  image: TouchImageView
        init {
            layout =  ItemView.findViewById(R.id.foto_local_item_layout)
            image =  ItemView.findViewById(R.id.foto_local_item_image)
        }
        fun bind(foto:FotoUploadModel){
            try {
                val fotoUri = Uri.parse(foto.idFile)
                    Glide.with(layout.context)
                        .load(fotoUri)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.imagem_falha)
                        .into(image)
            } catch (e:Exception){
                Log.i("zyzz","Erro-> ${e.message}")
            }

        }
    }


override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {

    val layoutInflater = LayoutInflater.from(parent.context)

    val itemView = layoutInflater.inflate(R.layout.item_foto_local,parent,false)

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
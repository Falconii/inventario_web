package com.simionato.inventarioweb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel

class ImoInventarioAdapter(
): RecyclerView.Adapter<ImoInventarioAdapter.InventarioViewHolder>() {

    var lista:List<ImobilizadoinventarioModel> = listOf()
    class InventarioViewHolder(val ItemView: View):RecyclerView.ViewHolder(ItemView) {
        val  layout:View
        val  txtDescricao : TextView
        val  txtSubTitulo: TextView

        init {
            layout       = ItemView.findViewById(R.id.llMainItemLista100)
            txtDescricao = ItemView.findViewById(R.id.txtDescricaoItemLista100)
            txtSubTitulo  = ItemView.findViewById(R.id.txtSubTituloItemLista100)

        }

        fun bind(imobilizadoInventario: ImobilizadoinventarioModel){

            txtDescricao.setText(imobilizadoInventario.id_imobilizado.toString())
            txtSubTitulo.setText(imobilizadoInventario.imo_descricao)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        val itemView = layoutInflater.inflate(R.layout.itemlista,parent,false)

        return InventarioViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return lista.size;
    }

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {
        val imobilizadoInventario = lista[position]

        holder.bind(imobilizadoInventario)
    }
}
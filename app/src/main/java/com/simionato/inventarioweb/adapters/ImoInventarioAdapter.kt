package com.simionato.inventarioweb.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel

class ImoInventarioAdapter(
    private val onClickc:(imobilizadoinventarioModel: ImobilizadoinventarioModel, idAcao:CadastrosAcoes, idx:Int) -> Unit
): RecyclerView.Adapter<ImoInventarioAdapter.InventarioViewHolder>() {

    private var lista = mutableListOf<ImobilizadoinventarioModel>()

    inner class InventarioViewHolder(val ItemView: View):RecyclerView.ViewHolder(ItemView) {
        val  layout:View
        val  txtSituacao : TextView
        val  txtDescricao : TextView
        val  txtCodigo:TextView
        val  txtCodigoNovo :TextView
        val  txtGrupo :TextView
        val  txtCC :TextView
        val  txtCCNovo :TextView
        val  txtTituloLancamento:TextView
        val  txtLancamento:TextView
        val  txtResponsavel:TextView
        val  txtObs :TextView
        val btLancamento:ImageButton
        val btFoto:ImageButton
        val btConsulta:ImageButton

        init {
            layout       = ItemView.findViewById(R.id.llI_item_inventario_master)
            txtSituacao     = ItemView.findViewById(R.id.item_inventario_situacao)
            txtCodigo     = ItemView.findViewById(R.id.item_inventario_codigo)
            txtCodigoNovo = ItemView.findViewById(R.id.item_inventario_codigo_novo)
            txtDescricao = ItemView.findViewById(R.id.item_inventario_descricao)
            txtGrupo = ItemView.findViewById(R.id.item_inventario_grupo)
            txtCC = ItemView.findViewById(R.id.item_inventario_cc)
            txtCCNovo = ItemView.findViewById(R.id.item_inventario_cc_novo)
            txtTituloLancamento = ItemView.findViewById(R.id.item_inventario_lancamento_view)
            txtLancamento = ItemView.findViewById(R.id.item_inventario_lancamento_nro_data)
            txtResponsavel = ItemView.findViewById(R.id.item_inventario_lancamento_resp)
            txtObs = ItemView.findViewById(R.id.item_inventario_obs)
            btLancamento = ItemView.findViewById(R.id.item_inventario_lancamento)
            btFoto = ItemView.findViewById(R.id.item_inventario_foto)
            btConsulta = ItemView.findViewById(R.id.item_inventario_consulta)
        }

        fun bind(imobilizadoInventario: ImobilizadoinventarioModel,idx:Int){



            txtSituacao.setText("${ParametroGlobal.Situacoes.getSituacao(
                imobilizadoInventario.status
            )}")
            txtCodigo.setText("CODIGO:${imobilizadoInventario.id_imobilizado.toString()}")
            txtCodigoNovo.visibility = if(imobilizadoInventario.new_codigo != 0) View.VISIBLE else View.GONE
            txtCodigoNovo.setText("COD. NOVO:${imobilizadoInventario.new_codigo.toString()}")
            txtDescricao.setText("DESCRIÇÃO:\n${imobilizadoInventario.imo_descricao}")
            txtGrupo.setText("Grupo:\n${imobilizadoInventario.grupo_descricao}")
            txtCC.setText("CC:\n${imobilizadoInventario.cc_descricao}")
            txtCCNovo.visibility = if(imobilizadoInventario.new_cc != "") View.VISIBLE else View.GONE
            txtCCNovo.setText("CC Novo:\n${imobilizadoInventario.new_cc_descricao}")

            var visivel =  if (imobilizadoInventario.id_lanca == 0){
                 View.GONE
            } else {
                 View.VISIBLE
            }

            txtTituloLancamento.visibility = visivel
            txtLancamento.visibility = visivel
            txtResponsavel.visibility = visivel
            txtObs.visibility = visivel

            txtTituloLancamento.setText("** INFORMAÇÕES DO LANÇAMENTO **")
            txtLancamento.setText("DATA: ${imobilizadoInventario.lanc_dt_lanca} Nro: ${imobilizadoInventario.id_lanca}")
            txtResponsavel.setText("Resp: ${imobilizadoInventario.usu_razao}")
            txtObs.setText("Observação:\n${imobilizadoInventario.lanc_obs}")

            btLancamento.setOnClickListener{
                onClickc(imobilizadoInventario,CadastrosAcoes.Lancamento,idx)
            }

            btFoto.setOnClickListener{
                onClickc(imobilizadoInventario,CadastrosAcoes.Foto,idx)
            }

            btConsulta.setOnClickListener{
                onClickc(imobilizadoInventario,CadastrosAcoes.Consulta,idx)
            }
        }
    }


    fun loadData( values: MutableList<ImobilizadoinventarioModel> ){
        lista = values
        notifyDataSetChanged()
    }

    fun updateData( item: ImobilizadoinventarioModel, idx:Int ){
        lista.set(idx,item)
        notifyItemChanged(idx)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        val itemView = layoutInflater.inflate(R.layout.item_inventario,parent,false)

        return InventarioViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return lista.size;
    }

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {
        val imobilizadoInventario = lista[position]

        holder.bind(imobilizadoInventario,position)
    }
}
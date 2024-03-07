package com.simionato.inventarioweb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel

class ImoInventarioAdapter(
    private val onClickc:(imobilizadoinventarioModel: ImobilizadoinventarioModel, idAcao:CadastrosAcoes, idx:Int) -> Unit
): RecyclerView.Adapter<ImoInventarioAdapter.InventarioViewHolder>() {

    private var lista = mutableListOf<ImobilizadoinventarioModel>()

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1
    private var totalPaginas :Int = 0
    private var paginaAtual:Int = 0
    inner class InventarioViewHolder(val ItemView: View, val view_Type : Int):RecyclerView.ViewHolder(ItemView) {

        lateinit var layout:View
        lateinit var  txtSituacao : TextView
        lateinit var  txtDescricao : TextView
        lateinit var  txtCodigo:TextView
        lateinit var  txtCodigoNovo :TextView
        lateinit var  txtGrupo :TextView
        lateinit var  txtCC :TextView
        lateinit var  txtCCNovo :TextView
        lateinit var  txtTituloLancamento:TextView
        lateinit var  txtLancamento:TextView
        lateinit var  txtResponsavel:TextView
        lateinit var  txtObs :TextView
        lateinit var btLancamento:ImageButton
        lateinit var btFoto:ImageButton
        lateinit var btConsulta:ImageButton

        lateinit var textViewProgressXX:TextView

        init {}

        fun bind(imobilizadoInventario: ImobilizadoinventarioModel,idx:Int){

            if (view_Type == VIEW_TYPE_ITEM)
            {

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
                when (imobilizadoInventario.status) {
                    0 -> {txtSituacao.getResources().getColor(R.color.corVermelho)}
                    1 -> {txtSituacao.getResources().getColor(R.color.corVerde)}
                    2..4 -> {txtSituacao.getResources().getColor(R.color.corAmarelo)}
                    5 -> {txtSituacao.getResources().getColor(R.color.black}
                    else -> {txtSituacao.getResources().getColor(R.color.corVermelho)}
                }

                txtSituacao.setText(
                    "${
                        ParametroGlobal.Situacoes.getSituacao(
                            imobilizadoInventario.status
                        )
                    }"
                )
                txtCodigo.setText("CODIGO:${imobilizadoInventario.id_imobilizado.toString()}")
                txtCodigoNovo.visibility =
                    if (imobilizadoInventario.new_codigo != 0) View.VISIBLE else View.GONE
                txtCodigoNovo.setText("COD. NOVO:${imobilizadoInventario.new_codigo.toString()}")
                txtDescricao.setText("DESCRIÇÃO:\n${imobilizadoInventario.imo_descricao}")
                txtGrupo.setText("Grupo:\n${imobilizadoInventario.grupo_descricao}")
                txtCC.setText("CC:\n${imobilizadoInventario.cc_descricao}")
                txtCCNovo.visibility =
                    if (imobilizadoInventario.new_cc != "") View.VISIBLE else View.GONE
                txtCCNovo.setText("CC Novo:\n${imobilizadoInventario.new_cc_descricao}")

                var visivel = if (imobilizadoInventario.id_lanca == 0) {
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

                btLancamento.setOnClickListener {
                    onClickc(imobilizadoInventario, CadastrosAcoes.Lancamento, idx)
                }

                btFoto.setOnClickListener {
                    onClickc(imobilizadoInventario, CadastrosAcoes.Foto, idx)
                }

                btConsulta.setOnClickListener {
                    onClickc(imobilizadoInventario, CadastrosAcoes.Consulta, idx)
                }
            }
            else {
                txtSituacao     = ItemView.findViewById(R.id.textViewProgressXX)
                txtSituacao.setText("Carregando Página ${paginaAtual+1}/${totalPaginas}")
            }
        }
    }

    fun setTotalPaginas(value:Int){
        totalPaginas = value
    }

    fun setPaginaAtual(value:Int){
        paginaAtual = value
    }

    fun loadData( values: MutableList<ImobilizadoinventarioModel> ){
        lista = values
        notifyDataSetChanged()
    }

    fun updateData( item: ImobilizadoinventarioModel, idx:Int ){
        lista.set(idx,item)
        notifyItemChanged(idx)
    }

    fun anexarData(values: MutableList<ImobilizadoinventarioModel>){
        lista.removeAt(lista.size -1)
        val idx = lista.size
        notifyItemRemoved(idx)
        lista.addAll(idx,values)
        notifyItemRangeInserted(idx,values.size-1)
    }

    fun getLastEmpresa():Int{
        return if (lista != null)  lista[lista.size-1].id_empresa else  1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {

        if (viewType == VIEW_TYPE_ITEM) {
        val layoutInflater = LayoutInflater.from(parent.context)

        val itemView = layoutInflater.inflate(R.layout.item_inventario,parent,false)

        return InventarioViewHolder(itemView,viewType)

        } else {
            val layoutInflater = LayoutInflater.from(parent.context)

            val itemView = layoutInflater.inflate(R.layout.item_carregando,parent,false)

            return InventarioViewHolder(itemView,viewType)
        }

    }

    override fun getItemCount(): Int {
        return lista.size;
    }

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {

        val imobilizadoInventario = lista[position]

        holder.bind(imobilizadoInventario,position)
    }

    override fun getItemViewType(position: Int): Int {
        return if (lista[position].id_empresa != 0) VIEW_TYPE_ITEM else VIEW_TYPE_LOADING
        //return super.getItemViewType(position)
    }

}
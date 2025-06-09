package com.simionato.inventarioweb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
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

        lateinit var  layout:View
        lateinit var  ll_item_inventario_situacao:View
        lateinit var  txtSituacao : TextView
        lateinit var  txtDescricao : TextView
        lateinit var  txtApelido : TextView
        lateinit var  txtPrincipal : TextView
        lateinit var  txtCodigo:TextView
        lateinit var  txtCodigoNovo :TextView
        lateinit var  txtGrupo :TextView
        lateinit var  txtCC :TextView
        lateinit var  txtCCNovo :TextView
        lateinit var  txtTituloLancamento:TextView
        lateinit var  txtLancamento:TextView
        lateinit var  txtResponsavel:TextView
        lateinit var  txtCondicaoBook:TextView
        lateinit var  txtObs :TextView
        lateinit var btLancamento:ImageButton
        lateinit var btFoto:ImageButton
        lateinit var btConsulta:ImageButton
        lateinit var llitem_inventario_progress:View

        init {}

        fun bind(imobilizadoInventario: ImobilizadoinventarioModel,idx:Int){

            if (view_Type == VIEW_TYPE_ITEM)
            {

                layout        = ItemView.findViewById(R.id.llI_item_inventario_master)
                ll_item_inventario_situacao = ItemView.findViewById(R.id.ll_item_inventario_situacao)
                txtSituacao   = ItemView.findViewById(R.id.item_inventario_situacao)
                txtPrincipal  = ItemView.findViewById(R.id.item_inventario_principal)
                txtCodigo     = ItemView.findViewById(R.id.item_inventario_codigo)
                txtCodigoNovo = ItemView.findViewById(R.id.item_inventario_codigo_novo)
                txtDescricao  = ItemView.findViewById(R.id.item_inventario_descricao)
                txtApelido    = ItemView.findViewById(R.id.item_inventario_apelido)
                txtGrupo      = ItemView.findViewById(R.id.item_inventario_grupo)
                txtCC         = ItemView.findViewById(R.id.item_inventario_cc)
                txtCCNovo     = ItemView.findViewById(R.id.item_inventario_cc_novo)
                txtTituloLancamento = ItemView.findViewById(R.id.item_inventario_lancamento_view)
                txtLancamento = ItemView.findViewById(R.id.item_inventario_lancamento_nro_data)
                txtResponsavel = ItemView.findViewById(R.id.item_inventario_lancamento_resp)
                txtCondicaoBook = ItemView.findViewById(R.id.item_inventario_lancamento_condicao_book)
                txtObs = ItemView.findViewById(R.id.item_inventario_obs)
                btLancamento = ItemView.findViewById(R.id.item_inventario_lancamento)
                btFoto = ItemView.findViewById(R.id.item_inventario_foto)
                btConsulta = ItemView.findViewById(R.id.item_inventario_consulta)
                llitem_inventario_progress = ItemView.findViewById(R.id.llitem_inventario_progress)

                llitem_inventario_progress.visibility = if ( imobilizadoInventario.id_imobilizado == 0 ) {View.VISIBLE} else {View.GONE}

                if ( llitem_inventario_progress.visibility == View.GONE) {

                    ll_item_inventario_situacao.visibility = View.VISIBLE
                    txtPrincipal.visibility = View.VISIBLE
                    txtCodigo.visibility = View.VISIBLE
                    txtCodigoNovo.visibility = View.VISIBLE
                    txtDescricao.visibility = View.VISIBLE
                    txtApelido.visibility = View.VISIBLE
                    txtGrupo.visibility = View.VISIBLE
                    txtCC.visibility = View.VISIBLE

                } else {
                    ll_item_inventario_situacao.visibility = View.GONE
                    txtPrincipal.visibility = View.GONE
                    txtCodigo.visibility = View.GONE
                    txtCodigoNovo.visibility = View.GONE
                    txtDescricao.visibility = View.GONE
                    txtApelido.visibility = View.GONE
                    txtGrupo.visibility = View.GONE
                    txtCC.visibility = View.GONE
                }
                when (imobilizadoInventario.status) {
                    0 -> {txtSituacao.setTextColor(ContextCompat.getColor(layout.context, R.color.corVermelho))}
                    1 -> {txtSituacao.setTextColor(ContextCompat.getColor(layout.context, R.color.corVerde))}
                    in 2..4 -> {txtSituacao.setTextColor(ContextCompat.getColor(layout.context, R.color.corAmarelo))}
                    5 -> {txtSituacao.setTextColor(ContextCompat.getColor(layout.context, R.color.secondary))}
                    else -> {txtSituacao.setTextColor(ContextCompat.getColor(layout.context, R.color.corVermelho))}
                }

                txtSituacao.setText(
                    "${
                        ParametroGlobal.Situacoes.getSituacao(
                            imobilizadoInventario.status
                        )
                    }"
                )

                if (imobilizadoInventario.imo_principal == 0){
                    txtPrincipal.setText("")
                } else {
                    txtPrincipal.setText(ParametroGlobal.prettyText.tituloDescricao("PRINC: ",imobilizadoInventario.imo_principal.toString().padStart(6,'0')+"-"+imobilizadoInventario.princ_descricao))
                }

                txtCodigo.setText(ParametroGlobal.prettyText.tituloDescricao("ATIVO: ",imobilizadoInventario.id_imobilizado.toString().padStart(6,'0')))

                txtCodigoNovo.visibility =
                    if (imobilizadoInventario.new_codigo != 0) View.VISIBLE else View.GONE
                txtCodigoNovo.setText(ParametroGlobal.prettyText.tituloDescricao("COD. NOVO: ",imobilizadoInventario.new_codigo.toString().padStart(6,'0')))

                txtDescricao.setText(ParametroGlobal.prettyText.tituloDescricao("DESCRIÇÃO: ",imobilizadoInventario.imo_descricao))
                txtApelido.setText(ParametroGlobal.prettyText.tituloDescricao("APELIDO: ",imobilizadoInventario.imo_apelido))
                txtGrupo.setText(ParametroGlobal.prettyText.tituloDescricao("GRUPO: ",imobilizadoInventario.grupo_descricao))
                txtCC.setText(ParametroGlobal.prettyText.tituloDescricao("CENTRO CUSTO: ",imobilizadoInventario.cc_descricao))
                txtCCNovo.visibility =
                    if (imobilizadoInventario.new_cc != "") View.VISIBLE else View.GONE
                txtCCNovo.setText(ParametroGlobal.prettyText.tituloDescricao("C.C. NOVO: ",imobilizadoInventario.new_cc_descricao))

                var visivel = if (imobilizadoInventario.id_lanca == 0) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                txtTituloLancamento.visibility = visivel
                txtLancamento.visibility = visivel
                txtResponsavel.visibility = visivel
                txtCondicaoBook.visibility = visivel
                txtObs.visibility = visivel

                txtTituloLancamento.setText("** INFORMAÇÕES DO LANÇAMENTO **")
                txtLancamento.setText(ParametroGlobal.prettyText.tituloDescricaoDois("DATA: ",imobilizadoInventario.lanc_dt_lanca,"Nº: ",imobilizadoInventario.id_lanca.toString()))
                txtResponsavel.setText(ParametroGlobal.prettyText.tituloDescricao("RESP: ",imobilizadoInventario.usu_razao))
                txtCondicaoBook.setText(ParametroGlobal.prettyText.tituloDescricaoDois("Condição: ",ParametroGlobal.Condicoes.getCondicao(imobilizadoInventario.condicao),"Book: ",ParametroGlobal.SimNao.getSimNao(imobilizadoInventario.book)))
                txtObs.setText(ParametroGlobal.prettyText.tituloDescricao("OBSERVAÇÃO: ",imobilizadoInventario.lanc_obs))
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

    fun clearData(){
        lista.clear()
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
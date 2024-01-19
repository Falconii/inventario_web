package com.simionato.inventarioweb

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityLancamentoBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.LancamentoModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import com.simionato.inventarioweb.services.ImobilizadoInventarioService
import com.simionato.inventarioweb.services.LancamentoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Date


class LancamentoActivity : AppCompatActivity() {
    private var params:ParametroImobilizadoInventario01 = ParametroImobilizadoInventario01(
        1,
        8,
        2,
        0,
        "",
        0,
        "",
        -1,
        "",
        0,
        0,
        0,
        50,
        "N",
        "Código",
        true
    )
    private var novo:Boolean = false
    private var ReadOnly = true


    private lateinit var inventario: ImobilizadoinventarioModel
    private val binding by lazy {
        ActivityLancamentoBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (ReadOnly){
            binding.txtViewSituacao02.setVisibility(View.GONE)
        }
        clearInventario()
        formulario(false)
        inicializar()

    }

    private fun inicializar(){
        inicializarTooBar()
        binding.imSearch.setOnClickListener{
            try {
                var codigo = binding.editCodigo.text.toString().toInt()
                CoroutineScope(Dispatchers.IO).launch {
                    getInventarios(codigo)
                }
            } catch ( e : NumberFormatException ){
                binding.editDescricao02.setText("Código Inválido!")
            }
        }
        binding.btCancelar02.setOnClickListener({})
        binding.btCancelar02.setOnClickListener({
            binding.editCodigo.setText("")
            formulario(false)})
        binding.btGravar02.setOnClickListener({
            val lancamento:LancamentoModel = loadInventario()
            CoroutineScope(Dispatchers.IO).launch {
                if (lancamento.id_lanca == 0){
                    saveApontamento(lancamento)
                } else {
                    updateApontamento(lancamento)
                }
            }
        })
    }
    fun getHoje():String{

        try {

            var date = Date()

            var data = ""

            val format = SimpleDateFormat("dd/MM/yyyy")

            data = format.format(date)

            return data

        } catch (e:Exception)
        {
            return ""
        }

    }
    fun loadInventario():LancamentoModel{
        try {
            var codigo = binding.editCodigoNovo02.text.toString().toInt()
            inventario.new_codigo = codigo
        } catch ( e : NumberFormatException ){
            inventario.new_codigo = 0
        }
        try {
            var codigo = binding.editNroLanc02.text.toString().toInt()
            inventario.id_lanca = codigo
        } catch ( e : NumberFormatException ){
            inventario.id_lanca = 0
        }
        inventario.lanc_dt_lanca = getHoje()
        inventario.new_cc = ""
        inventario.lanc_obs = binding.editObs.text.toString()
            var lancamento: LancamentoModel = LancamentoModel(
                inventario.id_empresa,
                inventario.id_filial,
                inventario.id_inventario,
                inventario.id_imobilizado,
                inventario.user_insert,
                inventario.id_lanca,
                inventario.lanc_obs,
                inventario.lanc_dt_lanca,
                inventario.lanc_estado,
                inventario.new_codigo,
                inventario.new_cc,
                1,
                if (inventario.id_lanca !== 0) 1 else 0,
                0,
                "",
                "",
                0,
                "",
                ""
            )
            Log.i("zyz","Indo para gravação ${lancamento}")

        return lancamento
    }
    private fun inicializarTooBar(){
        binding.ToolBar02.title = "Controle De Ativos"
        binding.ToolBar02.subtitle = "Inventário Intelli"
        binding.ToolBar02.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar02.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar02.inflateMenu(R.menu.menu_voltar)
        binding.ToolBar02.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.item_cancel_padrao -> {
                    finish()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }

       // setSupportActionBar(binding.ToolBar02)
    }
    private suspend fun getInventarios(cod:Int){
        var response: Response<List<ImobilizadoinventarioModel>>? = null
        params.id_imobilizado = 0
        params.new_codigo = 0
        if (binding.rbAntigo02.isChecked){
            params.id_imobilizado = cod
        } else {
            params.new_codigo = cod
        }
        Log.i("zyz","[PARAMETROS] ${params}")
        try {
            val imobilizadoInventarioService = InfraHelper.apiInventario.create( ImobilizadoInventarioService::class.java )
            response = imobilizadoInventarioService.getImobilizadosInventarios(params)

        }catch (e: Exception){
            e.printStackTrace()
            Log.e("zyz",e.message as String)
        }

        if ( response != null ){
            Log.i("zyz","[response] ${response.isSuccessful} ${response.code()}")
            if( response.isSuccessful ){
                var lancamentos = response.body()

                this.inventario = (lancamentos?.get(0) ?: ImobilizadoinventarioModel(
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    "",
                    0,
                    0,
                    "",
                    "",
                    0,
                    "",
                    "",
                    "" ,
                    "",
                    "",
                    0,
                    "",
                    "",
                    0,
                    "",
                    ""
                )) as ImobilizadoinventarioModel

                Log.e("zyz","[ACHEI] ${inventario}")

                withContext(Dispatchers.Main) {

                    with(binding) {
                        formulario(true)
                        binding.txtViewSituac.setText(ParametroGlobal.Situacoes.getSituacao(inventario.status))
                        binding.editNroLanc02.setText(
                            if (inventario.id_lanca == 0) "" else inventario.id_lanca.toString()
                        )
                        binding.editData02.setText(inventario.lanc_dt_lanca)
                        binding.editCodigoAtual02.setText(inventario.id_imobilizado.toString())
                        binding.editCodigoNovo02.setText(
                            if (inventario.new_codigo == 0) "" else inventario.new_codigo.toString()
                        )
                        binding.editDescricao02.setText(inventario.imo_descricao)
                        binding.editCCOriginal02.setText(inventario.cc_descricao)
                        binding.editCCNovol02.setText(
                            if (inventario.new_cc_descricao == "") "Ativo Não Transferido!" else inventario.new_cc_descricao
                        )
                        binding.editObs.setText(inventario.lanc_obs)
                    }
                }

            }else{
                val gson = Gson()
                val message = gson.fromJson(
                    response.errorBody()!!.charStream(),
                    HttpErrorMessage::class.java
                )
                inventario =ImobilizadoinventarioModel(0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    "",
                    0,
                    0,
                    message.getMessage().toString(),
                    "",
                    0,
                    "",
                    "",
                    "" ,
                    "",
                    "",
                    0,
                    "",
                    "",
                    0,
                    "",
                    "" )
                withContext(Dispatchers.Main) {
                    binding.editDescricao02.setText(inventario.imo_descricao)
                }
            }
        } else {
            Log.e("zyz","Falha Na Pesquisa!")
        }

    }
    private suspend fun saveApontamento(lancamento:LancamentoModel){
        var response: Response<LancamentoModel>? = null
        try {
            val lancamentoService = InfraHelper.apiInventario.create( LancamentoService::class.java )
            response = lancamentoService.insertLancamento(lancamento)

        }catch (e: Exception){
            e.printStackTrace()
            Log.e("zyz",e.message as String)
        }

        if ( response != null ){
            Log.i("zyz","[INSERT response] ${response.isSuccessful} ${response.code()}")
            if( response.isSuccessful ){

                var lanca = response.body()
                Log.e("zyz","[Gravei] ${lanca}")
                formulario(false)

            } else {
                val gson = Gson()
                val message = gson.fromJson(
                    response.errorBody()!!.charStream(),
                    HttpErrorMessage::class.java
                )
                Log.i("zyz","[INSERT response] ${response.isSuccessful} ${response.code()}\n${message.getMessage()}")
            }
        } else {
            Log.e("zyz","Falha Na Inclusão!")
        }

    }
    private suspend fun updateApontamento(lancamento:LancamentoModel){
        var response: Response<LancamentoModel>? = null
        Log.i("zyz","Indo para o update ${lancamento}")
        try {
            val lancamentoService = InfraHelper.apiInventario.create( LancamentoService::class.java )
            response = lancamentoService.editLancamento(lancamento)

        }catch (e: Exception){
            e.printStackTrace()
            Log.e("zyz",e.message as String)
        }

        if ( response != null ){
            Log.i("zyz","[INSERT response] ${response.isSuccessful} ${response.code()}")
            if( response.isSuccessful ){

                var lanca = response.body()
                Log.e("zyz","[Gravei] ${lanca}")
                formulario(false)

            } else {
                val gson = Gson()
                val message = gson.fromJson(
                    response.errorBody()!!.charStream(),
                    HttpErrorMessage::class.java
                )
                Log.i("zyz","[INSERT response] ${response.isSuccessful} ${response.code()}\n${message.getMessage()}")
            }
        } else {
            Log.e("zyz","Falha Na Alteraçãi!")
        }

    }
   private fun formulario(show:Boolean){
       if (show){
           binding.txtViewSituacao02.setText("lançamento de Inventário")
       }
       binding.txtViewSituacao02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaSituacao02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaData02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaCodigoAtual02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaCodigoNovo02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaDescricao02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaCCNovol02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaCCOriginal02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.txtInputObs02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       if (inventario.id_lanca == 0){
           binding.btExcluir02.visibility = View.GONE
           binding.btGravar02.text = "Incluir"
       } else {
           binding.btExcluir02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
           binding.btGravar02.text = "Alterar"
       }
       binding.btCancelar02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.btGravar02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       pesquisa(!show)
   }
    private fun pesquisa(show:Boolean){
        binding.llCodigo.visibility = if (!show) { View.GONE} else {View.VISIBLE}
    }

    private fun clearInventario(){
        inventario =ImobilizadoinventarioModel(0,
            0,
            0,
            0,
            0,
            0,
            0,
            "",
            0,
            0,
            "",
            "",
            0,
            "",
            "",
            "" ,
            "",
            "",
            0,
            "",
            "",
            0,
            "",
            "" )
    }





}
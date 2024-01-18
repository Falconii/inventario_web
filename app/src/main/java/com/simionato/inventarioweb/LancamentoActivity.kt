package com.simionato.inventarioweb

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityLancamentoBinding
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
import java.text.SimpleDateFormat
import java.util.Locale


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
    private var codigo:Int = 0
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
        binding.ToolBar02.inflateMenu(R.menu.menu_padrao)
        binding.ToolBar02.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.item_cancel_padrao -> {
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.item_ok_padrao -> {
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
                    inventario.lanc_dt_lanca = "18/01/2024"
                    inventario.new_cc = ""
                    inventario.lanc_obs = binding.editObs.text.toString()

                    if (inventario.id_lanca == 0) {
                        inventario.lanc_estado =1
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
                            inventario.user_insert,
                            inventario.user_update,
                            0,
                            "",
                            "",
                            0,
                            "",
                            ""
                        )
                        Log.i("zyz","Indo para gravação ${lancamento}")
                        //this.currentFocus?.let { view ->
                        //    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        //   imm?.hideSoftInputFromWindow(view.windowToken, 1)
                        //}
                        CoroutineScope(Dispatchers.IO).launch {
                            saveApontamento(lancamento)
                        }
                    }

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
        params.id_imobilizado = cod
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

                    binding.editNroLanc02.setText(
                        if (inventario.id_lanca == 0) "" else  inventario.id_lanca.toString()
                    )
                    binding.editData02.setText(inventario.lanc_dt_lanca)
                    binding.editCodigoAtual02.setText(inventario.id_imobilizado.toString())
                    binding.editCodigoNovo02.setText(
                        if (inventario.new_codigo == 0) "" else  inventario.new_codigo.toString()
                        )
                    binding.editDescricao02.setText(inventario.imo_descricao)
                    binding.editCCOriginal02.setText(inventario.cc_descricao)
                    binding.editCCNovol02.setText(
                        if (inventario.new_cc_descricao == "") "Ativo Não Transferido!" else  inventario.new_cc_descricao
                    )
                    binding.editObs.setText(inventario.lanc_obs)
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
                Toast.makeText(applicationContext,message.getMessage(),Toast.LENGTH_LONG).show()
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

}
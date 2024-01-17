package com.simionato.inventarioweb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityLancamentoBinding
import com.simionato.inventarioweb.databinding.ActivityLoginBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.CentroCustoModel
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.LancamentoModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import com.simionato.inventarioweb.parametros.ParametroInventario01
import com.simionato.inventarioweb.services.CentroCustoService
import com.simionato.inventarioweb.services.ImobilizadoInventarioService
import com.simionato.inventarioweb.services.InventarioService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

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
    private lateinit var lancamento: ImobilizadoinventarioModel
    private val binding by lazy {
        ActivityLancamentoBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.imSearch.setOnClickListener{
            try {
                var codigo = binding.editCodigo.text.toString().toInt()
                CoroutineScope(Dispatchers.IO).launch {
                    getLancamento(codigo)
                }
            } catch ( e : NumberFormatException ){
                binding.editDescricao02.setText("Código Inválido!")
            }

        }
    }

    private suspend fun getLancamento(cod:Int){
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

                //lancamentos?.forEach { la -> lancamento = la }

                this.lancamento = (lancamentos?.get(0) ?: ImobilizadoinventarioModel(
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
                    0 ,
                    "",
                    "",
                    0,
                    "",
                    "",
                    0,
                    "",
                    ""
                )) as ImobilizadoinventarioModel

                Log.e("zyz","[ACHEI] ${lancamento}")

                withContext(Dispatchers.Main) {
                    binding.editData02.setText(lancamento.lanc_dt_lanca)
                    binding.editDescricao02.setText(lancamento.imo_descricao)
                    binding.editCCOriginal02.setText(lancamento.cc_descricao)
                    binding.editCCNovol02.setText(lancamento.new_cc_descricao)
                    binding.editObs.setText(lancamento.lanc_obs)
                }
            }else{
                val gson = Gson()
                val message = gson.fromJson(
                    response.errorBody()!!.charStream(),
                    HttpErrorMessage::class.java
                )
                ImobilizadoinventarioModel(0,
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
                    0 ,
                    "",
                    "",
                    0,
                    "",
                    "",
                    0,
                    "",
                    "" )
                withContext(Dispatchers.Main) {
                    binding.editDescricao02.setText(lancamento.imo_descricao)
                }
            }
        } else {
            Log.e("zyz","Falha Na Pesquisa!")
        }

    }

}
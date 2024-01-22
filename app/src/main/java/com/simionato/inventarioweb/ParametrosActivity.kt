package com.simionato.inventarioweb

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityParametrosBinding
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.CentroCustoModel
import com.simionato.inventarioweb.models.InventarioModel
import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.parametros.ParametroCentroCusto01
import com.simionato.inventarioweb.parametros.ParametroInventario01
import com.simionato.inventarioweb.parametros.ParametroLocal01
import com.simionato.inventarioweb.services.CentroCustoService
import com.simionato.inventarioweb.services.InventarioService
import com.simionato.inventarioweb.services.LocalService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ParametrosActivity : AppCompatActivity() {

    val params : ParametroCentroCusto01 = ParametroCentroCusto01(
        1,
        1,
        "",
        "",
        0,
        50,
        "N",
        "Código",
        true
    )

    val paramLocal: ParametroLocal01 = ParametroLocal01(
        1,
        0,
        "",
        "",
        0,
        50,
        "N",
        "Código",
        true
    )

    private val paramInventario:ParametroInventario01 = ParametroInventario01(
        1,
        8,
        0,
        "",
        0,
        50,
        "N",
        "Código",
        true
    )
    private val binding by lazy {
        ActivityParametrosBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val texto = " A mutação de um tipo usado no DataStore invalida todas as garantias fornecidas e cria bugs potencialmente graves e difíceis de detectar. É altamente recomendável usar buffers de protocolo que ofereçam garantias de imutabilidade, uma API simples e uma serialização eficiente."
        binding.editEmpresa01.setText(texto)
        binding.editEmpresa01.setOnClickListener{
            Log.i("CLICADO","Entrei No Evento")
            Toast.makeText(this,"Olha a Empresa",Toast.LENGTH_LONG).show()
            if (binding.editEmpresa01.text.toString().contains("MARCOS RENATO FALCONI")){
                binding.editEmpresa01.setText("")
            } else {
                binding.editEmpresa01.setText("MARCOS RENATO FALCONI")
            }
            true
        }
        CoroutineScope(Dispatchers.IO).launch {
            getCC()
            getInventarios()
        }
        getLocais()
    }

    private suspend fun getCC(){
        var response: Response<List<CentroCustoModel>>? = null
        try {
            val ccService = InfraHelper.apiInventario.create( CentroCustoService::class.java )
            response = ccService.getCentrosCustos(params)

        }catch (e: Exception){
            e.printStackTrace()
            Log.e("cc",e.message as String)
        }

        if ( response != null ){
            Log.i("centrocusto","[response] ${response.isSuccessful} ${response.code()}")
            if( response.isSuccessful ){
                val centroscustos = response.body()
                centroscustos?.forEach({cc -> Log.i("centrocusto",cc.descricao)})
            }else{
                val gson = Gson()
                val message = gson.fromJson(
                    response.errorBody()!!.charStream(),
                    HttpErrorMessage::class.java
                )

               Log.i("centrocustos",message.getMessage().toString())
               Log.i("centrocusto","[CODE] ${response.code()}\n[BODY]${response.body()}\n[RAW]${response.raw()}\n[message]${response.message()}\n[BODY]${response.body()}\n[errorBody]${response.errorBody()}\n[headers]${response.headers()}")
            }
        } else {
            Log.e("centrocusto","Falha Na Pesquisa!")
        }

    }

    private fun getLocais(){
        try {
            val localService = InfraHelper.apiInventario.create( LocalService::class.java )

            localService.getLocais(paramLocal).enqueue(object : Callback<List<LocalModel>>{
                override fun onResponse(
                    call: Call<List<LocalModel>>,
                    response: Response<List<LocalModel>>
                ) {
                    if (response != null) {
                        if (response.isSuccessful) {
                            var locais = response.body()

                            Log.e("zyz", "[ACHEI] ${locais}")

                        } else {
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                        }

                    }
                }

                override fun onFailure(call: Call<List<LocalModel>>, t: Throwable) {
                }
            })

        }catch (e: Exception){
            e.printStackTrace()
            Log.e("cc",e.message as String)
        }

    }

    private suspend fun getInventarios(){
        var response: Response<List<InventarioModel>>? = null
        try {
            val inventarioService = InfraHelper.apiInventario.create( InventarioService::class.java )
            response = inventarioService.getInventarios(paramInventario)

        }catch (e: Exception){
            e.printStackTrace()
            Log.e("inventarios",e.message as String)
        }

        if ( response != null ){
            Log.i("inventarios","[RETORNO] ${response.isSuccessful} ${response.code()}")
            if( response.isSuccessful ){
                val inventarios = response.body()
                inventarios?.forEach({invent -> Log.i("inventarios",invent.descricao)})
            }else{
                val gson = Gson()
                val message = gson.fromJson(
                    response.errorBody()!!.charStream(),
                    HttpErrorMessage::class.java
                )

                Log.i("inventarios",message.getMessage().toString())
            }
        } else {
            Log.e("inventarios","Falha Na Pesquisa!")
        }

    }
}
package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.simionato.inventarioweb.adapters.InventarioAdapter
import com.simionato.inventarioweb.adapters.LocalAdapter
import com.simionato.inventarioweb.databinding.ActivityParametrosBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.InventarioModel
import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.models.PadraoModel
import com.simionato.inventarioweb.parametros.ParametroCentroCusto01
import com.simionato.inventarioweb.parametros.ParametroInventario01
import com.simionato.inventarioweb.parametros.ParametroLocal01
import com.simionato.inventarioweb.services.InventarioService
import com.simionato.inventarioweb.services.LocalService
import com.simionato.inventarioweb.services.PadraoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ParametrosActivity : AppCompatActivity() {

    val paramsCC : ParametroCentroCusto01 = ParametroCentroCusto01(
        1,
        8,
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
        0,
        0,
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

    var localMem:LocalModel = LocalModel()
    var inventarioMem:InventarioModel = InventarioModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.llProgress01.visibility = View.VISIBLE
        iniciar()
        if ( (ParametroGlobal.Dados.usuario.id_empresa == 0) || (ParametroGlobal.Dados.usuario.id == 0) ){
            showToast("Usuário Não Está Logado!! ${ParametroGlobal.Dados.usuario}")
            finish()
        } else {
            getLocais()
        }
    }

    private fun iniciar(){
        inicializarTooBar()
        binding.btCancelar01.setOnClickListener({
            val returnIntent: Intent = Intent()
            setResult(Activity.RESULT_CANCELED,returnIntent)
            finish()
        })

        binding.btGravar01.setOnClickListener{
            if (localMem.id == 0){
                showToast("Local Do Inventário Não Definido!")
                return@setOnClickListener
            }

            if (inventarioMem.codigo == 0){
                showToast("Inventário Não Definido!")
                return@setOnClickListener
            }
            ParametroGlobal.Dados.local.id = localMem.id
            ParametroGlobal.Dados.Inventario.codigo = inventarioMem.codigo

            savePadrao()

            return@setOnClickListener
        }

        localMem = ParametroGlobal.Dados.local

        inventarioMem = ParametroGlobal.Dados.Inventario
    }
    private fun inicializarTooBar(){
        binding.ToolBar02.title = "Parâmetros"
        binding.ToolBar02.subtitle = "Inventário Intelli"
        binding.ToolBar02.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar02.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )

        binding.ToolBar02.inflateMenu(R.menu.menu_parametros)
        binding.ToolBar02.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.item_cancel -> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_CANCELED,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    private fun getLocais(){
        try {
            val localService = InfraHelper.apiInventario.create( LocalService::class.java )
            paramLocal.id_empresa =ParametroGlobal.Dados.empresa.id
            binding.llProgress01.visibility = View.VISIBLE
            localService.getLocais(paramLocal).enqueue(object : Callback<List<LocalModel>>{
                override fun onResponse(
                    call: Call<List<LocalModel>>,
                    response: Response<List<LocalModel>>
                ) {
                    binding.llProgress01.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var locais = response.body()
                            loadLocais(locais!!)
                        } else {
                            binding.llProgress01.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            var locais: List<LocalModel> = listOf()
                            if (response.code() == 409){
                                loadLocais(locais)
                            } else {
                                loadLocais(locais)
                                showToast("${message.getMessage().toString()}",Toast.LENGTH_SHORT)
                            }
                        }

                    } else {
                        binding.llProgress01.visibility = View.GONE
                        var locais: List<LocalModel> = listOf()
                        loadLocais(locais)
                    }
                }

                override fun onFailure(call: Call<List<LocalModel>>, t: Throwable) {
                    binding.llProgress01.visibility = View.GONE
                    var locais: List<LocalModel> = listOf()
                    loadLocais(locais)
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress01.visibility = View.GONE
            var locais: List<LocalModel> = listOf()
            loadLocais(locais)
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun getInventarios(local:LocalModel){
        try {
            paramInventario.id_empresa = local.id_empresa
            paramInventario.id_filial =  local.id
            binding.llProgress01.visibility = View.VISIBLE
            val inventarioService = InfraHelper.apiInventario.create( InventarioService::class.java )
            inventarioService.getInventarios(paramInventario).enqueue(object : Callback<List<InventarioModel>> {
                override fun onResponse(
                    call: Call<List<InventarioModel>>,
                    response: Response<List<InventarioModel>>
                ) {
                    binding.llProgress01.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {
                            var inventarios = response.body()
                            loadInventarios(inventarios!!)
                        } else {
                            binding.llProgress01.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                var inventarios: List<InventarioModel> = listOf()
                                loadInventarios(inventarios)
                            } else {
                                var inventarios: List<InventarioModel> = listOf()
                                loadInventarios(inventarios)
                                showToast("${message.getMessage().toString()}",Toast.LENGTH_SHORT)
                            }
                        }

                    } else {
                        binding.llProgress01.visibility = View.GONE
                        var inventarios: List<InventarioModel> = listOf()
                        loadInventarios(inventarios)
                        showToast("Sem retorno Da Requisição!")
                    }
                }

                override fun onFailure(call: Call<List<InventarioModel>>, t: Throwable) {
                    binding.llProgress01.visibility = View.GONE
                    var inventarios: List<InventarioModel> = listOf()
                    loadInventarios(inventarios)
                    showToast(t.message.toString())
                }

            })

        }catch (e: Exception){
            binding.llProgress01.visibility = View.GONE
            var inventarios: List<InventarioModel> = listOf()
            loadInventarios(inventarios)
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }


    }

    private fun savePadrao(){

        try {
            var padrao = PadraoModel(
                ParametroGlobal.Dados.usuario.id_empresa,
                ParametroGlobal.Dados.usuario.id,
                ParametroGlobal.Dados.empresa.id,
                localMem.id,
                inventarioMem.codigo,
                ParametroGlobal.Dados.usuario.id,
                0,
                "",
                "",
                ""
            )
            val padraoService = InfraHelper.apiInventario.create( PadraoService::class.java )
            padraoService.insertUpdatePadrao(padrao).enqueue(object : Callback<PadraoModel> {
                override fun onResponse(call: Call<PadraoModel>, response: Response<PadraoModel>) {
                    binding.llProgress01.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {
                            var padrao = response.body()
                            if (padrao != null) {
                                showToast("Padrão Alterado!")
                                loadParametros(padrao)
                            } else {
                                showToast("Falha Na Alterção Do Padrão!")
                            }
                        } else {
                            binding.llProgress01.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            showToast("${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                        }
                    } else {
                        binding.llProgress01.visibility = View.GONE
                        showToast("Sem retorno Da Requisição!")
                    }
                }

                override fun onFailure(call: Call<PadraoModel>, t: Throwable) {
                    binding.llProgress01.visibility = View.GONE
                    showToast(t.message.toString())
                }

            })

        }catch (e: Exception){
            binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }


    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(baseContext, mensagem, duracao).show()
    }

    private fun loadLocais(locais:List<LocalModel>){

        val local  = locais.indexOfFirst{it.id == ParametroGlobal.Dados.local.id}

        val idxInicial = if (local == -1) { 0 } else { local }

        val adapter = LocalAdapter(this,R.layout.item_spinner_opcoes,locais)

        binding.spinnerLocal.adapter = adapter;

        binding.spinnerLocal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, p3: Long) {
                val local = binding.spinnerLocal.selectedItem as LocalModel
                localMem = local
                getInventarios(local!!)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        binding.spinnerLocal.setSelection(idxInicial)

    }

    private fun loadInventarios(inventarios:List<InventarioModel>){

        val inventario  = inventarios.indexOfFirst{ it.codigo == ParametroGlobal.Dados.Inventario.codigo}

        val idxInicial = if (inventario == -1 ){ 0 } else { inventario }

        val adapter = InventarioAdapter(this,R.layout.item_spinner_opcoes,inventarios)

        binding.spinnerInventario.adapter = adapter

        binding.spinnerInventario.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, p3: Long) {
                try {
                    val inventario = binding.spinnerInventario.selectedItem as InventarioModel
                    inventarioMem = inventario
                } catch (e:Exception){
                    showToast("${e.message}")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        binding.spinnerInventario.setSelection(idxInicial)

    }

    private fun loadParametros(padrao:PadraoModel){
        ParametroGlobal.Dados.local.id_empresa = padrao.id_empresa_padrao
        ParametroGlobal.Dados.local.id = padrao.id_local_padrao
        ParametroGlobal.Dados.Inventario.id_empresa = padrao.id_empresa_padrao
        ParametroGlobal.Dados.Inventario.id_filial = padrao.id_local_padrao
        ParametroGlobal.Dados.Inventario.codigo = padrao.id_inv_padrao
        getLocal()
    }

    private fun getLocal(){
        try {
            val localService = InfraHelper.apiInventario.create( LocalService::class.java )
            binding.llProgress01.visibility = View.VISIBLE
            localService.getLocal(
                ParametroGlobal.Dados.local.id_empresa,
                ParametroGlobal.Dados.local.id)
                .enqueue(object : Callback<LocalModel>{
                    override fun onResponse(
                        call: Call<LocalModel>,
                        response: Response<LocalModel>
                    ) {
                        binding.llProgress01.visibility = View.GONE
                        if (response != null) {
                            if (response.isSuccessful) {

                                var local = response.body()

                                local = (local ?: LocalModel()) as LocalModel

                                ParametroGlobal.Dados.local = local

                                getInventario()

                            } else {
                                binding.llProgress01.visibility = View.GONE
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409){
                                    showToast("Local Não Encontrado!")
                                } else {
                                    showToast("${message.getMessage().toString()}")
                                }
                            }

                        } else {
                            binding.llProgress01.visibility = View.GONE
                            showToast("Sem retorno Da Requisição!")
                        }
                    }

                    override fun onFailure(call: Call<LocalModel>, t: Throwable) {
                        binding.llProgress01.visibility = View.GONE
                        showToast(t.message.toString())
                    }
                })

        }catch (e: Exception){
            binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun getInventario(){
        try {
            val inventarioService = InfraHelper.apiInventario.create( InventarioService::class.java )
            inventarioService.getInventario(
                ParametroGlobal.Dados.Inventario.id_empresa,
                ParametroGlobal.Dados.Inventario.id_filial,
                ParametroGlobal.Dados.Inventario.codigo
            )
                .enqueue(object : Callback<InventarioModel> {
                    override fun onResponse(
                        call: Call<InventarioModel>,
                        response: Response<InventarioModel>
                    ) {
                        //binding.llProgress01.visibility = View.GONE
                        if (response != null) {
                            if (response.isSuccessful) {

                                var inventario = response.body()

                                inventario = (inventario ?: InventarioModel()) as InventarioModel

                                ParametroGlobal.Dados.Inventario = inventario

                                Log.i("zyz","Inventario: ${inventario}")

                                val returnIntent: Intent = Intent()
                                setResult(Activity.RESULT_OK,returnIntent)
                                finish()

                            } else {
                                //binding.llProgress01.visibility = View.GONE
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409){
                                    showToast("Inventário Não Encontrado! ${message.getMessage().toString()}",Toast.LENGTH_SHORT)
                                } else {
                                    showToast("${message.getMessage().toString()}",Toast.LENGTH_SHORT)
                                }
                            }

                        } else {
                            //binding.llProgress01.visibility = View.GONE
                            Toast.makeText(applicationContext,"Sem retorno Da Requisição!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<InventarioModel>, t: Throwable) {
                        //binding.llProgress01.visibility = View.GONE
                        showToast(t.message.toString())
                    }

                })

        }catch (e: Exception){
            binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

}
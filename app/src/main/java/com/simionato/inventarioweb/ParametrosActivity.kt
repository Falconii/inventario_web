package com.simionato.inventarioweb

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.simionato.inventarioweb.adapters.InventarioAdapter
import com.simionato.inventarioweb.adapters.LocalAdapter
import com.simionato.inventarioweb.databinding.ActivityParametrosBinding
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.InventarioModel
import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.parametros.ParametroCentroCusto01
import com.simionato.inventarioweb.parametros.ParametroInventario01
import com.simionato.inventarioweb.parametros.ParametroLocal01
import com.simionato.inventarioweb.services.InventarioService
import com.simionato.inventarioweb.services.LocalService
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
        binding.llProgress01.visibility = View.GONE
        inicializarTooBar()
        getLocais()
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
                            if (response.code() == 409){
                                showToast("CC Não Encontrado! ${message.getMessage().toString()}",Toast.LENGTH_SHORT)
                            } else {
                                showToast("CC Não Encontrado! ${message.getMessage().toString()}",Toast.LENGTH_SHORT)
                            }
                        }

                    } else {
                        binding.llProgress01.visibility = View.GONE
                        Toast.makeText(applicationContext,"Sem retorno Da Requisição!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<LocalModel>>, t: Throwable) {
                    binding.llProgress01.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun getInventarios(){
        try {
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
                                showToast("Inventário Não Encontrado! ${message.getMessage().toString()}",Toast.LENGTH_SHORT)
                            } else {
                                showToast("Inventário Não Encontrado! ${message.getMessage().toString()}",Toast.LENGTH_SHORT)
                            }
                        }

                    } else {
                        binding.llProgress01.visibility = View.GONE
                        Toast.makeText(applicationContext,"Sem retorno Da Requisição!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<InventarioModel>>, t: Throwable) {
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
        Toast.makeText(this, mensagem, duracao).show()
    }

    private fun loadLocais(locais:List<LocalModel>){

        binding.spinnerLocal.adapter = LocalAdapter(this,R.layout.item_spinner_opcoes,locais)

        binding.spinnerLocal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, p3: Long) {
                val local = binding.spinnerLocal.selectedItem as LocalModel
                getInventarios()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

    }

    private fun loadInventarios(inventarios:List<InventarioModel>){

        binding.spinnerInventario.adapter = InventarioAdapter(this,R.layout.item_spinner_opcoes,inventarios)

        binding.spinnerLocal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, p3: Long) {
                val inventario = binding.spinnerLocal.selectedItem as InventarioModel
                Toast.makeText(baseContext, "Descricao: ${inventario.descricao}",Toast.LENGTH_LONG).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }



    }
}
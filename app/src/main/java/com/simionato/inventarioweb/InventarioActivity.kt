package com.simionato.inventarioweb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.simionato.inventarioweb.adapters.ImoInventarioAdapter
import com.simionato.inventarioweb.databinding.ActivityInventarioBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import com.simionato.inventarioweb.services.ImobilizadoInventarioService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val adapter = ImoInventarioAdapter()

class InventarioActivity : AppCompatActivity() {

    private var params: ParametroImobilizadoInventario01 = ParametroImobilizadoInventario01()

    private lateinit var imobilizadoinventarios: List<ImobilizadoinventarioModel>

    private val binding by lazy {
        ActivityInventarioBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.llProgress40.visibility = View.GONE
        //Validando Paramentros
        if (ParametroGlobal.Ambiente.itsOK()){
            showToast("Ambiente Incorreto!!")
            finish()
            return
        }
        inicializar()
        }


    private fun inicializar(){

        inicializarTooBar()

        getInventarios()


    }

    private fun inicializarTooBar(){
        binding.ToolBar40.title = "Controle De Ativos"
        binding.ToolBar40.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar40.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar40.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar40.inflateMenu(R.menu.menu_inventario)
        binding.ToolBar40.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.menu_inventario_sair -> {
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_inventario_filtro -> {

                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    private fun getInventarios(){
        params.pagina = 1
        params.tamPagina = 1000
        try {
            val imobilizadoInventarioService = InfraHelper.apiInventario.create( ImobilizadoInventarioService::class.java )
            binding.llProgress40.visibility = View.VISIBLE
            imobilizadoInventarioService.getImobilizadosInventarios(params).enqueue(object :
                Callback<List<ImobilizadoinventarioModel>> {
                override fun onResponse(
                    call: Call<List<ImobilizadoinventarioModel>>,
                    response: Response<List<ImobilizadoinventarioModel>>
                ) {
                    binding.llProgress40.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            imobilizadoinventarios = response.body()!!

                            if (imobilizadoinventarios !== null) {

                                montaLista(imobilizadoinventarios);

                            } else {
                                showToast("Falha No Retorno Da Requisição!")
                            }

                        }
                        else {
                            binding.llProgress40.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                showToast("Tabela De Fotos Vazia")
                                imobilizadoinventarios = listOf()
                                montaLista(imobilizadoinventarios)
                            } else {
                                showToast(message.getMessage().toString())
                            }
                        }
                    } else {
                        binding.llProgress40.visibility = View.GONE
                        Toast.makeText(applicationContext,"Sem retorno Da Requisição!",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<ImobilizadoinventarioModel>>, t: Throwable) {
                    binding.llProgress40.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress40.visibility = View.GONE
            showToast("${e.message.toString()}",Toast.LENGTH_LONG)
        }

    }

    private fun  montaLista(imobilizados:List<ImobilizadoinventarioModel>){
        adapter.lista = imobilizados
        binding.rvLista40.adapter = adapter
        binding.rvLista40.layoutManager =
            LinearLayoutManager(binding.rvLista40.context)
        binding.rvLista40.addItemDecoration(
            DividerItemDecoration(
                binding.rvLista40.context,
                RecyclerView.VERTICAL
            )
        )
    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}


package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.simionato.inventarioweb.adapters.FotosAtivosAdapter
import com.simionato.inventarioweb.databinding.ActivityFotosAtivosBinding
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.showToast
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.parametros.ParametroFoto01
import com.simionato.inventarioweb.services.FotoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FotosAtivosActivity : AppCompatActivity() {

    private val adapter = FotosAtivosAdapter()

    private val binding by lazy { ActivityFotosAtivosBinding.inflate(layoutInflater) }

    private var imobilizado = 0;

    private var params: ParametroFoto01 = ParametroFoto01()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        //Validando Paramentros
        if ((ParametroGlobal.Dados.usuario.id == 0)   ||
            (ParametroGlobal.Dados.local.id   == 0)   ||
            (ParametroGlobal.Dados.Inventario.codigo == 0)){
            showToast(applicationContext,"Ambiente Incorreto!!")
            finish()
        }

        try {
            val bundle = intent.extras
            if (bundle != null) {
                imobilizado = bundle.getInt("imobilizado",0)
            } else {
                showToast(applicationContext,"Parâmetro Código Da Ativo Incorreto!!")
                finish()
            }
        } catch (error:Exception){
            showToast(applicationContext,"Erro Nos Parametros: ${error.message}")
            finish()
        }
        iniciar()
    }

    private fun iniciar(){
        inicializarToolbar()
        getFotos()
    }

    private fun inicializarToolbar() = with(binding.ToolBar105) {

        title    = "Controle De Ativos"
        subtitle = com.simionato.inventarioweb.global.ParametroGlobal.Dados.Inventario.descricao
        setTitleTextColor(androidx.core.content.ContextCompat.getColor(context, com.simionato.inventarioweb.R.color.white))
        setSubtitleTextColor(androidx.core.content.ContextCompat.getColor(context, com.simionato.inventarioweb.R.color.white))
        inflateMenu(com.simionato.inventarioweb.R.menu.menu_login)

        setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == com.simionato.inventarioweb.R.id.item_cancel) {
                setResult(android.app.Activity.RESULT_OK, Intent())
                finish()
            }
            true
        }
    }

    private fun encerrarComErro(mensagem: String) {
        showToast(applicationContext,mensagem)
        setResult(Activity.RESULT_CANCELED)
        finish()
    }


    private fun getFotos(){
        try {
            val fotoService = InfraHelper.apiInventario.create( FotoService::class.java )
            params.id_empresa       = ParametroGlobal.Dados.Inventario.id_empresa
            params.id_local         = ParametroGlobal.Dados.Inventario.id_filial
            params.id_inventario    = ParametroGlobal.Dados.Inventario.codigo
            params.id_imobilizado   = this.imobilizado
            params.destaque = ""
            binding.llProgress105.visibility = View.VISIBLE
            fotoService.getFotos(params).enqueue(object :
                Callback<List<FotoModel>> {
                override fun onResponse(
                    call: Call<List<FotoModel>>,
                    response: Response<List<FotoModel>>
                ) {
                    binding.llProgress105.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var fotos = response.body()

                            if (fotos !== null) {

                                montaLista(fotos);

                            } else {
                                showToast(applicationContext,"Falha No Retorno Da Requisição!")
                            }
                        }
                        else {
                            binding.llProgress105.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                showToast(applicationContext,"Tabela De Fotos Vazia")
                                val fotos:List<FotoModel> = listOf()
                                montaLista(fotos )
                            } else {
                                showToast(applicationContext,message.getMessage().toString(), Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress105.visibility = View.GONE

                    }
                }

                override fun onFailure(call: Call<List<FotoModel>>, t: Throwable) {
                    binding.llProgress105.visibility = View.GONE
                    showToast(applicationContext,t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress105.visibility = View.GONE
            showToast(applicationContext,e.message.toString(), Toast.LENGTH_LONG)
        }

    }


    private fun  montaLista(fotos:List<FotoModel>){
        adapter.lista = fotos
        binding.rvLista105.adapter = adapter
        binding.rvLista105.layoutManager =
            LinearLayoutManager(binding.rvLista105.context)
        binding.rvLista105.addItemDecoration(
            DividerItemDecoration(
                binding.rvLista105.context,
                RecyclerView.VERTICAL
            )
        )
    }

}
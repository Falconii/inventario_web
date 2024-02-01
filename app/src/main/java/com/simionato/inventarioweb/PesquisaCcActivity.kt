package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.simionato.inventarioweb.adapters.CentroAdapter
import com.simionato.inventarioweb.adapters.GrupoAdapter
import com.simionato.inventarioweb.databinding.ActivityPesquisaCcBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.CentroCustoModel
import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.parametros.ParametroCentroCusto01
import com.simionato.inventarioweb.services.CentroCustoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PesquisaCcActivity : AppCompatActivity() {

    val params : ParametroCentroCusto01 = ParametroCentroCusto01()

    private var ccs = listOf<CentroCustoModel>()

    private val binding by lazy {
        ActivityPesquisaCcBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        iniciar()
    }

    private fun iniciar(){
        getCcs()
    }
    private fun inicializarTooBar(){
        binding.ToolBar11.title = "Controle De Ativos"
        binding.ToolBar11.subtitle = "Pesquisa Centro De Custos"
        binding.ToolBar11.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar11.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )

        binding.ToolBar11.inflateMenu(R.menu.menu_parametros)
        binding.ToolBar11.setOnMenuItemClickListener { menuItem ->
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
    private fun getCcs(){
        try {
            val centroCustoService = InfraHelper.apiInventario.create( CentroCustoService::class.java )
            params.id_empresa = ParametroGlobal.Dados.empresa.id
            params.id_filial = ParametroGlobal.Dados.local.id
            binding.llProgress11.visibility = View.VISIBLE
            centroCustoService.getCentrosCustos(params).enqueue(object :
                Callback<List<CentroCustoModel>> {
                override fun onResponse(
                    call: Call<List<CentroCustoModel>>,
                    response: Response<List<CentroCustoModel>>
                ) {
                    binding.llProgress11.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var cc = response.body()

                            if (cc !== null) {

                                val adapter = CentroAdapter(cc){centro ->
                                    val returnIntent: Intent = Intent()
                                    returnIntent.putExtra("codigo",centro.codigo)
                                    returnIntent.putExtra("descricao",centro.descricao)
                                    setResult(Activity.RESULT_OK,returnIntent)
                                    finish()
                                }
                                binding.rvLista11.adapter = adapter
                                binding.rvLista11.layoutManager =
                                    LinearLayoutManager(binding.rvLista11.context)
                                binding.rvLista11.addItemDecoration(
                                    DividerItemDecoration(
                                        binding.rvLista11.context,
                                        RecyclerView.VERTICAL
                                    )
                                )

                                binding.svPesquisa11.setOnQueryTextListener(object :
                                    SearchView.OnQueryTextListener {
                                    override fun onQueryTextSubmit(p0: String?): Boolean {
                                        return false
                                    }

                                    override fun onQueryTextChange(newText: String?): Boolean {

                                        adapter.filter.filter(newText)

                                        return false
                                    }

                                })
                            } else {
                                showToast("Falha No Retorno Da Requisição!")
                            }

                        }
                        else {
                            binding.llProgress11.visibility = View.GONE
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                            var locais: List<LocalModel> = listOf()
                            if (response.code() == 409){
                                showToast("Tabela De Centro De Custos Vazia")
                            } else {
                                showToast("${message.getMessage().toString()}",Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress11.visibility = View.GONE
                        var locais: List<LocalModel> = listOf()

                    }
                }

                override fun onFailure(call: Call<List<CentroCustoModel>>, t: Throwable) {
                    binding.llProgress11.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress11.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}
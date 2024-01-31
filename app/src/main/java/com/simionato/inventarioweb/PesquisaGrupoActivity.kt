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
import com.simionato.inventarioweb.adapters.GrupoAdapter
import com.simionato.inventarioweb.adapters.PesquisaAdapter
import com.simionato.inventarioweb.databinding.ActivityPesquisaBinding
import com.simionato.inventarioweb.databinding.ActivityPesquisaGrupoBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.GrupoModel
import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.parametros.ParametroGrupo01
import com.simionato.inventarioweb.services.GrupoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PesquisaGrupoActivity : AppCompatActivity() {

    val params : ParametroGrupo01 = ParametroGrupo01()

    private var grupos = listOf<GrupoModel>()

    private val binding by lazy {
        ActivityPesquisaGrupoBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        iniciar()
    }

    private fun iniciar(){
        inicializarTooBar()
        getGrupos()
    }

    private fun inicializarTooBar(){
        binding.ToolBar10.title = "Controle De Ativos"
        binding.ToolBar10.subtitle = "Pesquisa Grupos De Produtos"
        binding.ToolBar10.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar10.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )

        binding.ToolBar10.inflateMenu(R.menu.menu_parametros)
        binding.ToolBar10.setOnMenuItemClickListener { menuItem ->
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
    private fun getGrupos(){
        try {
            val grupoService = InfraHelper.apiInventario.create( GrupoService::class.java )
            params.id_empresa = ParametroGlobal.Dados.empresa.id
            params.id_filial = ParametroGlobal.Dados.local.id
            binding.llProgress10.visibility = View.VISIBLE
            grupoService.getGrupos(params).enqueue(object :
                Callback<List<GrupoModel>> {
                override fun onResponse(
                    call: Call<List<GrupoModel>>,
                    response: Response<List<GrupoModel>>
                ) {
                    binding.llProgress10.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var grupos = response.body()

                            showToast("Grupos Carregados")

                            if (grupos !== null) {

                                grupos.forEach { it -> Log.i("zyzz", it.descricao) }

                                val adapter = GrupoAdapter(grupos)
                                binding.rvLista10.adapter = adapter
                                binding.rvLista10.layoutManager =
                                    LinearLayoutManager(binding.rvLista10.context)
                                binding.rvLista10.addItemDecoration(
                                    DividerItemDecoration(
                                        binding.rvLista10.context,
                                        RecyclerView.VERTICAL
                                    )
                                )

                                binding.svPesquisa10.setOnQueryTextListener(object :
                                    SearchView.OnQueryTextListener {
                                    override fun onQueryTextSubmit(p0: String?): Boolean {
                                        return false
                                    }

                                    override fun onQueryTextChange(newText: String?): Boolean {

                                        adapter.filter.filter(newText)

                                        return false
                                    }

                                })
                            }

                        } else {
                            binding.llProgress10.visibility = View.GONE
                            try {
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                            } catch (e:Exception){
                                Log.e("zyzz","erro: ${e.message}")
                            }
                            var locais: List<LocalModel> = listOf()
                            if (response.code() == 409){
                                //loadLocais(locais)
                            } else {
                                //showToast("${message.getMessage().toString()}",Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress10.visibility = View.GONE
                        var locais: List<LocalModel> = listOf()

                    }
                }

                override fun onFailure(call: Call<List<GrupoModel>>, t: Throwable) {
                    binding.llProgress10.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress10.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}
package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.simionato.inventarioweb.adapters.CentroAdapter
import com.simionato.inventarioweb.adapters.PrincipalAdapter
import com.simionato.inventarioweb.databinding.ActivityPesquisaCcBinding
import com.simionato.inventarioweb.databinding.ActivityPesquisaPrincipalBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.CentroCustoModel
import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.models.PrincipalModel
import com.simionato.inventarioweb.parametros.ParametroCentroCusto01
import com.simionato.inventarioweb.parametros.ParametroPrincipal01
import com.simionato.inventarioweb.services.CentroCustoService
import com.simionato.inventarioweb.services.PrincipalService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PesquisaPrincipalActivity : AppCompatActivity() {

    val params : ParametroPrincipal01 = ParametroPrincipal01()

    private var principais = listOf<PrincipalModel>()

    private val binding by lazy {
        ActivityPesquisaPrincipalBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        iniciar()
    }

    private fun iniciar(){
        inicializarTooBar()
        getPrincipais()
    }
    private fun inicializarTooBar(){
        binding.ToolBar56.title = "Controle De Ativos"
        binding.ToolBar56.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar56.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar56.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar56.inflateMenu(R.menu.menu_pesquisa)
        binding.ToolBar56.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.menu_pesquisa_sair -> {
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
    private fun getPrincipais(){
        try {
            val principalService = InfraHelper.apiInventario.create( PrincipalService::class.java )
            params.id_empresa = ParametroGlobal.Dados.empresa.id
            params.id_filial = ParametroGlobal.Dados.local.id
            binding.llProgress56.visibility = View.VISIBLE
            principalService.getPrincipais(params).enqueue(object :
                Callback<List<PrincipalModel>> {
                override fun onResponse(
                    call: Call<List<PrincipalModel>>,
                    response: Response<List<PrincipalModel>>
                ) {
                    binding.llProgress56.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var principaisResp = response.body()

                            if (principaisResp !== null) {
                                principais = principaisResp;

                                val adapter = PrincipalAdapter(principais){principal ->
                                    val returnIntent: Intent = Intent()
                                    returnIntent.putExtra("codigo",principal.codigo)
                                    returnIntent.putExtra("descricao",principal.descricao)
                                    setResult(RESULT_OK,returnIntent)
                                    finish()
                                }
                                binding.rvLista56.adapter = adapter
                                binding.rvLista56.layoutManager =
                                    LinearLayoutManager(binding.rvLista56.context)
                                binding.rvLista56.addItemDecoration(
                                    DividerItemDecoration(
                                        binding.rvLista56.context,
                                        RecyclerView.VERTICAL
                                    )
                                )

                                binding.svPesquisa56.setOnQueryTextListener(object :
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
                            binding.llProgress56.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            principais = listOf()
                            if (response.code() == 409){
                                showToast("Tabela De Principais Vazia")
                            } else {
                                showToast("${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress56.visibility = View.GONE
                        principais = listOf()

                    }
                }

                override fun onFailure(call: Call<List<PrincipalModel>>, t: Throwable) {
                    binding.llProgress56.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress56.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}
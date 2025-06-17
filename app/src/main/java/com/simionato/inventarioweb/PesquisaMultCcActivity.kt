package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
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
import com.simionato.inventarioweb.adapters.CentroMultiAdapter
import com.simionato.inventarioweb.databinding.ActivityPesquisaCcBinding
import com.simionato.inventarioweb.databinding.ActivityPesquisaMultCcBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.CentroCustoModel
import com.simionato.inventarioweb.models.CentroCustoMultiModel
import com.simionato.inventarioweb.parametros.ParametroCentroCusto01
import com.simionato.inventarioweb.services.CentroCustoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import okhttp3.internal.notifyAll
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PesquisaMultCcActivity : AppCompatActivity() {

    val params : ParametroCentroCusto01 = ParametroCentroCusto01()

    private var ccs = listOf<CentroCustoModel>()

    private var ccsMuli = mutableListOf<CentroCustoMultiModel>()

    private val binding by lazy {
        ActivityPesquisaMultCcBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: CentroMultiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pesquisa_mult_cc)


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(binding.root)
        iniciar()

    }

    private fun iniciar(){
        inicializarTooBar()

        getCcs()


        binding.checkbox200.setOnCheckedChangeListener { _, isChecked ->

            if (!(adapter == null)) {
                ccsMuli.forEach { cc -> cc.check = isChecked }

                adapter.notifyDataSetChanged()
            }
        }
    }
    private fun inicializarTooBar(){
        binding.ToolBar200.title = "Controle De Ativos"
        binding.ToolBar200.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar200.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar200.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar200.inflateMenu(R.menu.menu_pesquisamultipla)
        binding.ToolBar200.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.menu_pesquisa_multipla_sair-> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_CANCELED,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_pesquisa_multipla_ok -> {
                    val listaCodigos = ccsMuli.filter { it.check }
                    if (listaCodigos.count() == 0){
                        showToast("Escolha Pelo Menos Um Centro De Custo")
                        return@setOnMenuItemClickListener true
                    }
                    val codigo = if (listaCodigos.count() == 1) { listaCodigos.get(0).centro_custo.codigo} else listaCodigos.joinToString(";") { it.centro_custo.codigo}
                    println(codigo)
                    val returnIntent = Intent()
                    returnIntent.putExtra("codigo", codigo)
                    returnIntent.putExtra("descricao", if (listaCodigos.count() == 1) { listaCodigos.get(0).centro_custo.descricao} else "Multiplos Centros De Custos")
                    setResult(Activity.RESULT_OK, returnIntent)
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
            binding.llProgress200.visibility = View.VISIBLE
            centroCustoService.getCentrosCustos(params).enqueue(object :
                Callback<List<CentroCustoModel>> {
                override fun onResponse(
                    call: Call<List<CentroCustoModel>>,
                    response: Response<List<CentroCustoModel>>
                ) {
                    binding.llProgress200.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var ccsResp = response.body()

                            if (ccsResp !== null) {

                                ccs = ccsResp

                                ccsMuli = mutableListOf()

                                ccs.forEach{cc -> ccsMuli.add(CentroCustoMultiModel(false,cc))}

                                adapter = CentroMultiAdapter(ccsMuli, { centro ->
                                    val returnIntent = Intent()
                                    returnIntent.putExtra("codigo", centro.centro_custo.codigo)
                                    returnIntent.putExtra("descricao", centro.centro_custo.descricao)
                                    setResult(Activity.RESULT_OK, returnIntent)
                                    finish()
                                }, { centro, isChecked ->
                                    // Aqui você pode adicionar a lógica para lidar com a seleção do checkbox
                                    println("Item ${centro.centro_custo.codigo} foi ${if (isChecked) "marcado" else "desmarcado"}")
                                })
                                binding.rvLista200.adapter = adapter
                                binding.rvLista200.layoutManager =
                                    LinearLayoutManager(binding.rvLista200.context)
                                binding.rvLista200.addItemDecoration(
                                    DividerItemDecoration(
                                        binding.rvLista200.context,
                                        RecyclerView.VERTICAL
                                    )
                                )

                                binding.svPesquisa200.setOnQueryTextListener(object :
                                    SearchView.OnQueryTextListener,
                                    androidx.appcompat.widget.SearchView.OnQueryTextListener {
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
                            binding.llProgress200.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            ccs = listOf()
                            ccsMuli = mutableListOf()
                            if (response.code() == 409){
                                showToast("Tabela De Centro De Custos Vazia")
                            } else {
                                showToast("${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress200.visibility = View.GONE
                        ccs = listOf()
                        ccsMuli = mutableListOf()

                    }
                }

                override fun onFailure(call: Call<List<CentroCustoModel>>, t: Throwable) {
                    binding.llProgress200.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress200.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}